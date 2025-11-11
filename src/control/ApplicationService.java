package control;

import java.util.*;
import java.util.stream.Collectors;

import entity.Application;
import entity.CompanyRepresentative;
import entity.InternshipOpportunity;
import entity.Student;
import enumerations.ApplicationStatus;
import enumerations.OfferStatus;
import enumerations.OpportunityStatus;

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
    public boolean hasActiveApplication(Student student, InternshipOpportunity opp) {
        return applications.stream().anyMatch(a ->
                a.getStudent().getId().equalsIgnoreCase(student.getId())
                        && a.getOpportunity().getId().equalsIgnoreCase(opp.getId())
                        && a.isActive());
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

    /** rep reviews an application → approve (issue offer) or reject; uses markDecision(...) */
    public void decideApplication(CompanyRepresentative rep, Application app, boolean approve) {
        InternshipOpportunity opp = app.getOpportunity();

        // verify company rep owns this opportunity
        if (!rep.equals(opp.getRepInCharge())) {
            System.out.println("✗ You may only review applications for your own opportunities.");
            return;
        }

        // only allow issuing offers if opportunity is approved and not full
        if (approve) {
            if (opp.getStatus() != OpportunityStatus.APPROVED) {
                System.out.println("✗ Opportunity is not approved; cannot issue offers.");
                return;
            }
            if (opp.getConfirmedSlots() >= opp.getSlots()) {
                System.out.println("✗ Opportunity is already full; cannot issue offers.");
                return;
            }
        }

        // only pending applications can be decided
        if (app.getStatus() != ApplicationStatus.PENDING) {
            System.out.println("✗ This application has already been decided (" + app.getStatus() + ").");
            return;
        }

        // make the decision (this already sets ApplicationStatus and OfferStatus correctly)
        app.markDecision(approve);

        if (approve) {
            // successful: offer created for student → offerStatus = PENDING
            System.out.println("✓ Application marked as successful. Offer sent to student.");
        } else {
            // unsuccessful: no offer for student → offerStatus = NOT_APPLICABLE
            System.out.println("✓ Application marked as unsuccessful.");
        }

        // confirmedSlots should increase only after student accepts
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
        app.setOfferStatus(OfferStatus.ACCEPTED);
        app.markAccepted();
        opp.setConfirmedSlots(opp.getConfirmedSlots() + 1);
        opp.setSlots(opp.getSlots() - 1); // reduce available slots by 1

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

        app.setOfferStatus(OfferStatus.REJECTED);
        // mark as withdrawn using your existing function
        app.markWithdrawn();

        System.out.println("✓ offer rejected successfully.");
        save(); // persist changes if needed
    }
}