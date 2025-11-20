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
    private List<InternshipOpportunity> opportunities;

    public ApplicationService(List<Application> applications, List<InternshipOpportunity> opportunities, List<WithdrawalRequest> withdrawalRequests, DataLoader loader) {
        this.applications = Objects.requireNonNull(applications);
        this.loader = Objects.requireNonNull(loader);
        this.withdrawalRequests = Objects.requireNonNull(withdrawalRequests);
        this.opportunities = Objects.requireNonNull(opportunities);
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
        loader.saveOpportunities(opportunities);
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

        if (approve) {
            if (opp.getConfirmedSlots() >= opp.getSlots()) {
                System.out.println("✗ No more slots available. Application is automatically rejected.");
                app.markDecision(false);
                return;
            }
        }

        app.markDecision(approve);

        System.out.println(approve
                ? "✓ Application marked as successful."
                : "✓ Application marked as unsuccessful.");
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

        String oppId = app.getOpportunity().getId();
        InternshipOpportunity opp = findMasterOpportunity(oppId);


        if (opp.getConfirmedSlots() >= opp.getSlots()) {
            System.out.println("✗ No remaining slots available for this opportunity.");
            return;
        }

        app.markAccepted();
        opp.incrementConfirmedSlots();

        if (opp.getConfirmedSlots() >= opp.getSlots()) {
            opp.setStatus(OpportunityStatus.FILLED);
        }

        withdrawOtherOffers(student, app);

        save();

        System.out.println();
        System.out.println("✓ Offer accepted. All other active applications withdrawn automatically.");
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

        app.markWithdrawn();

        System.out.println("✓ Offer rejected successfully.");
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
            System.out.println("✗ You can only request withdrawal for active applications.");
            return false;
        }

        if (hasPendingWithdrawal(application)) {
            System.out.println("✗ A pending withdrawal request already exists for this application.");
            return false;
        }

        String id = "WR-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        WithdrawalRequest request = new WithdrawalRequest(id, application, student);
        withdrawalRequests.add(request);

        save();

        System.out.println("✓ Withdrawal request submitted successfully (pending approval by career center staff).");
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
        
        Application reqApp = req.getApplication();

        Application app = findMasterApplication(reqApp);

        String oppId = app.getOpportunity().getId();
        InternshipOpportunity opp = findMasterOpportunity(oppId);

        System.out.printf(
            app.getId(), app.getStatus(), app.isAccepted(),
            opp != null ? opp.getId() : "null",
            opp != null ? opp.getConfirmedSlots() : -1,
            opp != null ? opp.getSlots() : -1
        );

        req.review(staff, approve);

        if (approve) {

            if (app.getStatus() == ApplicationStatus.SUCCESSFUL && app.isAccepted()) {
                if (opp != null) {
                    opp.decrementConfirmedSlots();

                    if (opp.getStatus() == OpportunityStatus.FILLED && opp.hasVacancy()) {
                        opp.setStatus(OpportunityStatus.APPROVED);
                    }
                } 
            }

            app.markWithdrawn();


            System.out.printf(
                app.getId(), app.getStatus(), app.isAccepted(),
                opp != null ? opp.getId() : "null",
                opp != null ? opp.getConfirmedSlots() : -1,
                opp != null ? opp.getSlots() : -1
            );

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

    public List<Application> getApplicationsByStudent(Student student) {
        if (student == null) return Collections.emptyList();
        
        return applications.stream()
                .filter(app -> app.getStudent().equals(student))
                .collect(Collectors.toList());
    }

    private InternshipOpportunity findMasterOpportunity(String oppId) {
        if (oppId == null) return null;
        return opportunities.stream()
                .filter(o -> o.getId().equalsIgnoreCase(oppId))
                .findFirst()
                .orElse(null);
    }

    private Application findMasterApplication(Application app) {
        if (app == null) return null;
        return applications.stream()
                .filter(a -> a.getId().equals(app.getId()))
                .findFirst()
                .orElse(app);
    }
}