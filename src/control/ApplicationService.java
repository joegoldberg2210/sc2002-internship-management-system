package control;

import java.util.*;
import java.util.stream.Collectors;

import entity.Application;
import entity.CareerCenterStaff;
import entity.CompanyRepresentative;
import entity.InternshipOpportunity;
import entity.Student;
import entity.WithdrawalRequest;
import enumerations.ApplicationStatus;
import enumerations.OpportunityStatus;
import enumerations.WithdrawalStatus;

public class ApplicationService {
    private static final int MAX_ACTIVE_APPS = 3;

    private final List<Application> applications;
    private final DataLoader loader;

    public ApplicationService(List<Application> applications, DataLoader loader) {
        this.applications = Objects.requireNonNull(applications);
        this.loader = Objects.requireNonNull(loader);
    }

    /** student applies for an opportunity; returns the created application or null on failure */
    public Application applyForOpportunity(Student student, InternshipOpportunity opp) {
        if (student == null || opp == null) return null;

        // must be visible, within window, have vacancy, and match student's major/level
        if (!opp.isOpenFor(student)) return null;

        // cap at 3 active applications
        if (getActiveCountForStudent(student.getId()) >= MAX_ACTIVE_APPS) return null;

        // no duplicate active application for the same internship
        if (hasActiveApplication(student, opp)) return null;

        // create + persist
        String appId = "APP-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Application app = new Application(appId, student, opp); // defaults to PENDING
        applications.add(app);
        save();
        return app;
    }

    /** public: count student's active (pending) applications */
    public long getActiveCountForStudent(String studentId) {
        return applications.stream()
                .filter(a -> a.getStudent().getId().equalsIgnoreCase(studentId) && a.isActive())
                .count();
    }

    /** public: does student already have an active app for this exact opportunity? */
    public boolean hasActiveApplication(Student student, InternshipOpportunity opportunity) {
        return getApplicationsForStudent(student).stream()
            .anyMatch(a -> a.getOpportunity().equals(opportunity)
                    && (a.getStatus() == ApplicationStatus.PENDING
                        || a.getStatus() == ApplicationStatus.SUCCESSFUL));
    }

    /** public: fetch all applications for this student */
    public List<Application> getApplicationsForStudent(Student student) {
        return applications.stream()
                .filter(a -> a.getStudent().getId().equalsIgnoreCase(student.getId()))
                .toList();
    }

    private void save() {
        loader.saveApplications(applications);
    }

    public List<Application> getSuccessfulOffersForStudent(Student student) {
        return applications.stream()
                .filter(a -> a.getStudent().equals(student))
                .filter(a -> a.getStatus() == ApplicationStatus.SUCCESSFUL)
                .collect(Collectors.toList());
    }

    
    /** list applications tied to opportunities owned by this rep */
    public List<Application> getApplicationsByRepresentative(CompanyRepresentative rep) {
        return applications.stream()
                .filter(a -> a.getOpportunity() != null
                        && a.getOpportunity().getRepInCharge() != null
                        && a.getOpportunity().getRepInCharge().equals(rep))
                .collect(Collectors.toList());
    }

    /** rep reviews an application → approve (offer) or reject; uses markDecision(...) */
    public void decideApplication(CompanyRepresentative rep, Application app, boolean approve) {
        InternshipOpportunity opp = app.getOpportunity();

        if (!rep.equals(opp.getRepInCharge())) {
            System.out.println("✗ you may only review applications for your own opportunities.");
            return;
        }

        // only allow offers on approved & not-filled opportunities
        if (approve) {
            if (opp.getStatus() != OpportunityStatus.APPROVED) {
                System.out.println("✗ opportunity is not approved; cannot issue offers.");
                return;
            }
            if (opp.getConfirmedSlots() >= opp.getSlots()) {
                System.out.println("✗ opportunity is already full; cannot issue offers.");
                return;
            }
        }

        // only pending applications can be decided
        if (app.getStatus() != ApplicationStatus.PENDING) {
            System.out.println("✗ this application has already been decided (" + app.getStatus() + ").");
            return;
        }

        // make the decision via your existing api
        app.markDecision(approve);

        // do NOT change confirmedSlots here; only when student accepts
        System.out.println(approve
                ? "✓ application marked as successful (offer made)."
                : "✓ application marked as unsuccessful.");
        save();
    }

    /** student accepts an offer; uses markAccepted(); also updates opportunity capacity */
    public void acceptOffer(Student student, Application app) {
        if (app.getStatus() != ApplicationStatus.SUCCESSFUL) {
            System.out.println("✗ Cannot accept — Application is not successful.");
            return;
        }

        // ensure student hasn’t accepted another
        boolean alreadyAccepted = applications.stream()
                .anyMatch(a -> a.getStudent().equals(student) && a.isAccepted());
        if (alreadyAccepted) {
            System.out.println("✗ You have already accepted an internship offer. You cannot accept or reject other internship offer(s).");
            return;
        }

        InternshipOpportunity opp = app.getOpportunity();

        if (opp.getConfirmedSlots() >= opp.getSlots()) {
            System.out.println("✗ No remaining slots available for this opportunity.");
            return;
        }

        // accept this offer
        app.markAccepted();
        opp.setConfirmedSlots(opp.getConfirmedSlots() + 1);
        // opp.setSlots(opp.getSlots() - 1); // reduce available slots by 1

        // mark all other offers/applications by this student as withdrawn
        withdrawOtherOffers(student, app);

        // if filled → mark opportunity as filled and hide
        if (opp.getConfirmedSlots() >= opp.getSlots()) {
            opp.setStatus(OpportunityStatus.FILLED);
            opp.setVisibility(false);
        }

        System.out.println();
        System.out.println("✓ Offer accepted. All other active applications withdrawn automatically.");
        save();
    }

    private void withdrawOtherOffers(Student student, Application acceptedApp) {
        for (Application a : applications) {
            if (a.getStudent().equals(student) && !a.equals(acceptedApp)) {
                if (a.getStatus() == ApplicationStatus.PENDING || 
                    a.getStatus() == ApplicationStatus.SUCCESSFUL) {
                    a.markWithdrawn();
                }
            }
        }
    }

    /** student rejects / declines an internship offer */
    public void rejectOffer(Student student, Application app) {

        // must be a valid offer (successful but not yet accepted)
        if (app.getStatus() != ApplicationStatus.SUCCESSFUL) {
            System.out.println("✗ cannot reject — this application is not a valid offer.");
            return;
        }

        if (app.isAccepted()) {
            System.out.println("✗ cannot reject — you have already accepted this offer.");
            return;
        }

        // mark as withdrawn using your existing function
        app.markWithdrawn();

        System.out.println("✓ offer rejected successfully.");
        save(); // persist changes if needed
    }

    private final List<WithdrawalRequest> withdrawalRequests = new ArrayList<>();

    /** check if an application already has a pending withdrawal request */
    public boolean hasPendingWithdrawal(Application application) {
        return withdrawalRequests.stream()
                .anyMatch(req -> req.getApplication().equals(application)
                        && req.getStatus() == WithdrawalStatus.PENDING);
    }

    /** student submits a withdrawal request */
    public boolean submitWithdrawalRequest(Student student, Application application) {
        if (student == null || application == null) return false;
        if (!application.getStudent().equals(student)) return false;

        // only allow if application is still active (pending or successful)
        if (application.getStatus() != ApplicationStatus.PENDING
                && application.getStatus() != ApplicationStatus.SUCCESSFUL) {
            System.out.println("✗ you can only request withdrawal for active applications.");
            return false;
        }

        // block duplicates
        if (hasPendingWithdrawal(application)) {
            System.out.println("✗ a pending withdrawal request already exists for this application.");
            return false;
        }

        String id = "WR-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        WithdrawalRequest request = new WithdrawalRequest(id, application, student);
        withdrawalRequests.add(request);

        System.out.println("✓ withdrawal request submitted successfully (pending approval by career center staff).");
        return true;
    }

    /** list all pending withdrawal requests for staff view */
    public List<WithdrawalRequest> getPendingWithdrawalRequests() {
        return withdrawalRequests.stream()
                .filter(req -> req.getStatus() == WithdrawalStatus.PENDING)
                .collect(Collectors.toList());
    }

    /** staff reviews (approves / rejects) a withdrawal request */
    public void reviewWithdrawalRequest(CareerCenterStaff staff, WithdrawalRequest req, boolean approve) {
        if (req == null) return;
        if (req.isReviewed()) {
            System.out.println("✗ this request has already been reviewed.");
            return;
        }

        req.review(staff, approve);
        Application app = req.getApplication();
        InternshipOpportunity opp = app.getOpportunity();

        if (approve) {
            // if accepted, free up a slot
            if (app.isAccepted() && opp.getConfirmedSlots() > 0) {
                opp.setConfirmedSlots(opp.getConfirmedSlots() - 1);

                // reopen if previously filled
                if (opp.getStatus() == OpportunityStatus.FILLED
                        && opp.getConfirmedSlots() < opp.getSlots()) {
                    opp.setStatus(OpportunityStatus.APPROVED);
                    opp.setVisibility(true);
                }
            }

            app.markWithdrawn();
            System.out.println("✓ withdrawal request approved. application withdrawn.");
        } else {
            System.out.println("✓ withdrawal request rejected.");
        }

        save();
    }

    /** helper: view all withdrawal requests by a specific student */
    public List<WithdrawalRequest> getRequestsForStudent(Student student) {
        return withdrawalRequests.stream()
                .filter(r -> r.getRequestedBy().equals(student))
                .collect(Collectors.toList());
    }
}