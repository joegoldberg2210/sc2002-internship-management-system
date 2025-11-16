package entity;
import config.DomainRules;

import enumerations.ApplicationStatus;
import enumerations.InternshipLevel;
import enumerations.Major;

import java.util.ArrayList;
import java.util.List;

/** Student user with applications and saved preferences. */
public class Student extends User {
    private final int yearOfStudy;               // 1..4
    private Major major;                  // CSC, EEE, ...
    private final List<Application> applications = new ArrayList<>();
    private Application acceptedApplication;     // 0..1

    public Student(String id, String name, int yearOfStudy, Major major) {
        super(id, name);
        this.yearOfStudy = yearOfStudy;
        this.major = major;
    }

    public int getYearOfStudy() { return yearOfStudy; }
    public Major getMajor()    { return major; }
    public List<Application> getApplications() { return applications; }
    public Application getAcceptedApplication() { return acceptedApplication; }
    void _setAcceptedApplication(Application app) { this.acceptedApplication = app; }

    /** at most 3 concurrent (PENDING) applications. */
    public boolean hasCapacityForNewApplication() {
        return activeApplicationsCount() < 3;
    }

    /** Business rule: capacity + policy-driven eligibility + opportunity open window. */
    public boolean canApplyTo(InternshipOpportunity opp) {
        if (!hasCapacityForNewApplication()) return false;
        if (!opp.isOpenFor(this)) return false; // this checks status/visibility/window/vacancy
        return config.DomainRules.eligibility().canApply(this, opp);
}

    /** Students can view visible & eligible opportunities, or any opp they already applied to. */
    public boolean canView(InternshipOpportunity opp) {
        if (opp.isVisible() && opp.isEligibleFor(this)) return true;
        for (Application a : applications) if (a.getOpportunity() == opp) return true;
        return false;
    }

    // ----- helper kept private so public API matches the diagram -----
    private int activeApplicationsCount() {
        int c = 0;
        for (Application a : applications) if (a.getStatus() == ApplicationStatus.PENDING) c++;
        return c;
    }

    public Application createApplication(String id, InternshipOpportunity opp) {
    if (!canApplyTo(opp)) throw new IllegalStateException("Not eligible / no capacity");
    Application a = new Application(id, this, opp);
    this._addApplication(a);
    return a;
}

    /* package-private helpers for services */
    void _addApplication(Application a) { applications.add(a); }
    void _withdrawOthersExcept(Application accepted) {
        for (Application a : applications) if (a != accepted) a.markWithdrawn();
    }
}
