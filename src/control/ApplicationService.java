package control;

import java.util.*;
import java.util.stream.Collectors;

import entity.Application;
import entity.CompanyRepresentative;
import entity.InternshipOpportunity;
import entity.Student;
import enumerations.ApplicationStatus;
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

        // typical rule: only allow offers on approved opportunities
        if (approve && opp.getStatus() != OpportunityStatus.APPROVED) {
            System.out.println("✗ opportunity is not approved; cannot issue offers.");
            return;
        }

        // do not allow decisions on already decided applications
        if (app.getStatus() != ApplicationStatus.PENDING) {
            System.out.println("✗ this application has already been decided (" + app.getStatus() + ").");
            return;
        }

        // make the decision via your existing method
        app.markDecision(approve);

        // note: we DO NOT change confirmedSlots here.
        // confirmedSlots should rise only when the student explicitly accepts (markAccepted).
        System.out.println(approve ? "✓ application marked as successful (offer made)."
                                   : "✓ application marked as unsuccessful.");
        save();
    }

    /** student accepts an offer; uses markAccepted(); also updates opportunity capacity */
    public void acceptOffer(Student student, Application app) {
        if (!student.equals(app.getStudent())) {
            System.out.println("✗ you can only accept your own application.");
            return;
        }

        // must be a successful offer first
        if (app.getStatus() != ApplicationStatus.SUCCESSFUL) {
            System.out.println("✗ cannot accept — application is not successful.");
            return;
        }

        // enforce: a student may accept only one offer
        boolean alreadyAccepted = applications.stream()
                .anyMatch(a -> a.getStudent().equals(student) && a.isAccepted());
        if (alreadyAccepted) {
            System.out.println("✗ you have already accepted another offer.");
            return;
        }

        InternshipOpportunity opp = app.getOpportunity();

        // must have capacity remaining
        if (opp.getConfirmedSlots() >= opp.getSlots()) {
            System.out.println("✗ no remaining slots available for this opportunity.");
            return;
        }

        // ok: accept and update capacity
        app.markAccepted();
        opp.setSlots(opp.getConfirmedSlots() + 1);

        // if filled by this acceptance → flip status + visibility off
        if (opp.getConfirmedSlots() >= opp.getSlots()) {
            opp.setStatus(OpportunityStatus.FILLED);
            opp.setVisibility(false);
        }

        System.out.println("✓ offer accepted.");
        save();
    }
}