package entity;

import enumerations.ApplicationStatus;
import enumerations.Major;

import java.util.ArrayList;
import java.util.List;

public class Student extends User {
    private final int yearOfStudy;              
    private Major major;                  
    private final List<Application> applications = new ArrayList<>();
    private Application acceptedApplication;   

    public Student(String id, String name, int yearOfStudy, Major major) {
        super(id, name);
        this.yearOfStudy = yearOfStudy;
        this.major = major;
    }

    public int getYearOfStudy() { 
        return yearOfStudy; 
    }

    public Major getMajor()    { 
        return major; 
    }

    public List<Application> getApplications() { 
        return applications; 
    }

    public Application getAcceptedApplication() { 
        return acceptedApplication; 
    }

    void _setAcceptedApplication(Application app) { 
        this.acceptedApplication = app; 
    }

    /** 
     * @return boolean
     */
    public boolean hasCapacityForNewApplication() {
        return activeApplicationsCount() < 3;
    }

    /** 
     * @param opp
     * @return boolean
     */
    public boolean canApplyTo(InternshipOpportunity opp) {
        if (!hasCapacityForNewApplication()) return false;
        if (!opp.isOpenFor(this)) return false; 
        return config.DomainRules.eligibility().canApply(this, opp);
}

    /** 
     * @param opp
     * @return boolean
     */
    public boolean canView(InternshipOpportunity opp) {
        if (opp.isVisible() && opp.isEligibleFor(this)) return true;
        for (Application a : applications) if (a.getOpportunity() == opp) return true;
        return false;
    }

    /** 
     * @return int
     */
    private int activeApplicationsCount() {
        int c = 0;
        for (Application a : applications) if (a.getStatus() == ApplicationStatus.PENDING) c++;
        return c;
    }

    /** 
     * @param id
     * @param opp
     * @return Application
     */
    public Application createApplication(String id, InternshipOpportunity opp) {
    if (!canApplyTo(opp)) throw new IllegalStateException("Not eligible / no capacity");
    Application a = new Application(id, this, opp);
    this._addApplication(a);
    return a;
}

    /** 
     * @param accepted
     */
    void _addApplication(Application a) { applications.add(a); }
    /** 
     * @param accepted
     */
    void _withdrawOthersExcept(Application accepted) {
        for (Application a : applications) if (a != accepted) a.markWithdrawn();
    }
}
