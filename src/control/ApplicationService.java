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
    private final List<WithdrawalRequest> withdrawalRequests;

    public ApplicationService(List<Application> applications, DataLoader loader) {
        this.applications = Objects.requireNonNull(applications);
        this.loader = Objects.requireNonNull(loader);
        this.withdrawalRequests = new ArrayList<>(loader.loadWithdrawalRequests());
    }

    /** 
     * @param student
     * @param opp
     * @return Application
     */
    public Application applyForOpportunity(Student student, InternshipOpportunity opp) {
        if (student == null || opp == null) return null;

        if (!opp.isOpenFor(student)) return null;

        if (getActiveCountForStudent(student.getId()) >= MAX_ACTIVE_APPS) return null;

        if (hasActiveApplication(student, opp)) return null;

        String appId = "APP-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Application app = new Application(appId, student, opp); 
        applications.add(app);
        save();
        return app;
    }

    /** 
     * @param studentId
     * @return long
     */
    public long getActiveCountForStudent(String studentId) {
        return applications.stream()
                .filter(a -> a.getStudent().getId().equalsIgnoreCase(studentId) && a.isActive())
                .count();
    }

    /** 
     * @param student
     * @param opportunity
     * @return boolean
     */
    public boolean hasActiveApplication(Student student, InternshipOpportunity opportunity) {
        if (student == null || opportunity == null) return false;

        String sid = student.getId();
        String oid = opportunity.getId();

        return applications.stream()
                .anyMatch(a ->
                        a.getStudent() != null &&
                        a.getOpportunity() != null &&
                        a.getStudent().getId().equalsIgnoreCase(sid) &&
                        a.getOpportunity().getId().equalsIgnoreCase(oid) &&
                        (a.getStatus() == ApplicationStatus.PENDING
                                || a.getStatus() == ApplicationStatus.SUCCESSFUL)
                );
    }

    /** 
     * @param student
     * @return list&lt;application&gt;
     */
    public List<Application> getApplicationsForStudent(Student student) {
        return applications.stream()
                .filter(a -> a.getStudent().getId().equalsIgnoreCase(student.getId()))
                .toList();
    }

    private void save() {
        loader.saveApplications(applications);
        loader.saveWithdrawalRequests(withdrawalRequests);
    }

    /** 
     * @param student
     * @return list&lt;application&gt;
     */
    public List<Application> getSuccessfulOffersForStudent(Student student) {
        return applications.stream()
                .filter(a -> a.getStudent().equals(student))
                .filter(a -> a.getStatus() == ApplicationStatus.SUCCESSFUL)
                .collect(Collectors.toList());
    }

    
    /** 
     * @param rep
     * @return list&lt;application&gt;
     */
    public List<Application> getApplicationsByRepresentative(CompanyRepresentative rep) {
        return applications.stream()
                .filter(a -> a.getOpportunity() != null
                        && a.getOpportunity().getRepInCharge() != null
                        && a.getOpportunity().getRepInCharge().equals(rep))
                .collect(Collectors.toList());
    }

    /** 
     * @param rep
     * @param app
     * @param approve
     */
    public void decideApplication(CompanyRepresentative rep, Application app, boolean approve) {
        InternshipOpportunity opp = app.getOpportunity();

        if (!rep.equals(opp.getRepInCharge())) {
            System.out.println("✗ you may only review applications for your own opportunities.");
            return;
        }

        if (approve) {
            if (opp.getConfirmedSlots() >= opp.getSlots()) {
                System.out.println("✗ No more slots available. Application is automatically rejected.");
                app.markDecision(false);
                return;
            }
        }

        app.markDecision(approve);

        System.out.println(approve
                ? "✓ application marked as successful (offer made)."
                : "✓ application marked as unsuccessful.");
        save();
    }

    /** 
     * @param student
     * @param app
     */
    public void acceptOffer(Student student, Application app) {
        if (app.getStatus() != ApplicationStatus.SUCCESSFUL) {
            System.out.println("✗ Cannot accept — Application is not successful.");
            return;
        }

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

        app.markAccepted();
        opp.incrementConfirmedSlots();

        withdrawOtherOffers(student, app);


        if (opp.getConfirmedSlots() >= opp.getSlots()) {
            opp.setStatus(OpportunityStatus.FILLED);
            opp.setVisibility(false);
        }

        System.out.println();
        System.out.println("✓ Offer accepted. All other active applications withdrawn automatically.");
        save();
    }

    /** 
     * @param student
     * @param acceptedApp
     */
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

    /** 
     * @param student
     * @param app
     */
    public void rejectOffer(Student student, Application app) {

        if (app.getStatus() != ApplicationStatus.SUCCESSFUL) {
            System.out.println("✗ cannot reject — this application is not a valid offer.");
            return;
        }

        if (app.isAccepted()) {
            System.out.println("✗ cannot reject — you have already accepted this offer.");
            return;
        }

        app.markWithdrawn();

        System.out.println("✓ offer rejected successfully.");
        save();
    }

    /** 
     * @param application
     * @return boolean
     */
    public boolean hasPendingWithdrawal(Application application) {
        return withdrawalRequests.stream()
                .anyMatch(req -> req.getApplication().equals(application)
                        && req.getStatus() == WithdrawalStatus.PENDING);
    }

    /** 
     * @param student
     * @param application
     * @return boolean
     */
    public boolean submitWithdrawalRequest(Student student, Application application) {
        if (student == null || application == null) return false;
        if (!application.getStudent().equals(student)) return false;

        if (application.getStatus() != ApplicationStatus.PENDING
                && application.getStatus() != ApplicationStatus.SUCCESSFUL) {
            System.out.println("✗ you can only request withdrawal for active applications.");
            return false;
        }

        if (hasPendingWithdrawal(application)) {
            System.out.println("✗ a pending withdrawal request already exists for this application.");
            return false;
        }

        String id = "WR-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        WithdrawalRequest request = new WithdrawalRequest(id, application, student);
        withdrawalRequests.add(request);

        save();

        System.out.println("✓ withdrawal request submitted successfully (pending approval by career center staff).");
        return true;
    }

    /** 
     * @return list&lt;withdrawalrequest&gt;
     */
    public List<WithdrawalRequest> getPendingWithdrawalRequests() {
        return withdrawalRequests.stream()
                .filter(req -> req.getStatus() == WithdrawalStatus.PENDING)
                .collect(Collectors.toList());
    }

    /** 
     * @param staff
     * @param req
     * @param approve
     */
    public void reviewWithdrawalRequest(CareerCenterStaff staff, WithdrawalRequest req, boolean approve) {
        if (req == null) return;

        req.review(staff, approve);

        Application app = req.getApplication();
        InternshipOpportunity opp = app.getOpportunity();

        if (approve) {
            if (app.getStatus() == ApplicationStatus.SUCCESSFUL && app.isAccepted()) {
                opp.decrementConfirmedSlots(); 

                if (opp.getStatus() == OpportunityStatus.FILLED && opp.hasVacancy()) {
                    opp.setStatus(OpportunityStatus.APPROVED);
                    opp.setVisibility(true);
                }
            }

            app.markWithdrawn();

            System.out.println("✓ Withdrawal request approved. Application withdrawn successfully.");
        } else {
            System.out.println("✓ Withdrawal request rejected. Application remains active.");
        }

        save();
    }

    /** 
     * @param student
     * @param opportunity
     * @return boolean
     */
    public boolean hasAnyApplicationForOpportunity(Student student, InternshipOpportunity opportunity) {
        if (student == null || opportunity == null) return false;

        String sid = student.getId();
        String oid = opportunity.getId();

        return applications.stream()
                .anyMatch(a ->
                        a.getStudent() != null &&
                        a.getOpportunity() != null &&
                        a.getStudent().getId().equalsIgnoreCase(sid) &&
                        a.getOpportunity().getId().equalsIgnoreCase(oid) &&
                        a.getStatus() != ApplicationStatus.WITHDRAWN
                );
    }

    /** 
     * @param student
     * @return list&lt;withdrawalrequest&gt;
     */
    public List<WithdrawalRequest> getRequestsForStudent(Student student) {
        return withdrawalRequests.stream()
                .filter(r -> r.getRequestedBy().equals(student))
                .collect(Collectors.toList());
    }

    /** 
     * @return list&lt;withdrawalrequest&gt;
     */
    public List<WithdrawalRequest> getAllWithdrawalRequests() {
        return new ArrayList<>(withdrawalRequests);
    }
}