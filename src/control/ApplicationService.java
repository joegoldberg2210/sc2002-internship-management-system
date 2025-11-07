package control;

import java.util.*;
import java.util.stream.Collectors;

import entity.*;
import enumerations.*;
import java.util.UUID;

public class ApplicationService {

    private final List<Application> applications;
    private final List<InternshipOpportunity> opportunities;
    private final DataLoader loader;
    public static final int MAX_ACTIVE_APPS = 3;

    public ApplicationService(List<Application> applications, List<InternshipOpportunity> opportunities, DataLoader loader) {
        this.applications = Objects.requireNonNull(applications, "applications must not be null");
        this.opportunities = Objects.requireNonNull(opportunities, "opportunities must not be null");
        this.loader = Objects.requireNonNull(loader, "loader must not be null");
    }

    // -------------------- student actions --------------------

    /** student applies for an opportunity */
    public Application apply(Student student, InternshipOpportunity opp) {
        if (student == null || opp == null) {
            System.out.println("✗ invalid data.");
            return null;
        }

        // check if opportunity visible & open for this student
        if (!opp.isOpenFor(student)) {
            System.out.println("✗ this opportunity is not available for you (visibility, level, or major mismatch).");
            return null;
        }

        // limit: max 3 concurrent active applications
        long active = getActiveCountForStudent(student.getId());
        if (active >= MAX_ACTIVE_APPS) {
            System.out.println("✗ you already have " + active + " active application(s). max is " + MAX_ACTIVE_APPS + ".");
            return null;
        }

        // avoid duplicate active application
        boolean exists = applications.stream()
                .anyMatch(a -> a.getStudent().getId().equalsIgnoreCase(student.getId())
                        && a.getOpportunity().getId().equalsIgnoreCase(opp.getId())
                        && a.isActive());
        if (exists) {
            System.out.println("✗ you already have an active application for this opportunity.");
            return null;
        }

        // all checks passed
        String appId = "app-" + UUID.randomUUID().toString().substring(0, 6);
        Application app = new Application(appId, student, opp);
        applications.add(app);
        save();
        System.out.println("✓ application submitted successfully: " + app.getId());
        return app;
    }

    /** student withdraws application */
    public void withdraw(Student student, Application app) {
        if (app == null || student == null) {
            System.out.println("✗ invalid data.");
            return;
        }
        if (!app.getStudent().equals(student)) {
            System.out.println("✗ you can only withdraw your own applications.");
            return;
        }
        if (!app.isActive()) {
            System.out.println("✗ cannot withdraw a completed application.");
            return;
        }

        app.markWithdrawn();
        save();
        System.out.println("✓ application withdrawn.");
    }

    /** student accepts a successful offer */
    public void acceptPlacement(Student student, Application app) {
        if (app == null || student == null) {
            System.out.println("✗ invalid data.");
            return;
        }
        if (!app.getStudent().equals(student)) {
            System.out.println("✗ you can only accept your own offer.");
            return;
        }
        if (app.getStatus() != ApplicationStatus.SUCCESSFUL) {
            System.out.println("✗ only successful offers can be accepted.");
            return;
        }

        app.markAccepted();
        app.getOpportunity().incrementConfirmedSlots();
        save();
        System.out.println("✓ placement accepted.");
    }

    // -------------------- company rep / staff actions --------------------

    /** company rep decides application */
    public void decideApplication(CompanyRepresentative rep, Application app, boolean approve) {
        if (rep == null || app == null) {
            System.out.println("✗ invalid data.");
            return;
        }

        InternshipOpportunity opp = app.getOpportunity();
        if (!rep.equals(opp.getRepInCharge())) {
            System.out.println("✗ you can only review applications for your own opportunities.");
            return;
        }

        app.markDecision(approve);
        if (approve) {
            System.out.println("✓ application approved.");
        } else {
            System.out.println("✓ application rejected.");
        }
        save();
    }

    public List<Application> getByStudent(String studentId) {
        return applications.stream()
                .filter(a -> a.getStudent().getId().equalsIgnoreCase(studentId))
                .collect(Collectors.toList());
    }

    public List<Application> getByOpportunity(String oppId) {
        return applications.stream()
                .filter(a -> a.getOpportunity().getId().equalsIgnoreCase(oppId))
                .collect(Collectors.toList());
    }

    public long getActiveCountForStudent(String studentId) {
        return applications.stream()
                .filter(a -> a.getStudent().getId().equalsIgnoreCase(studentId))
                .filter(a -> a.isActive() && !a.isAccepted())
                .count();
    }

    private void save() {
        loader.saveApplications(applications);
        loader.saveOpportunities(opportunities);
    }
}