package policy;

import entity.InternshipOpportunity;
import entity.Student;
import enumerations.InternshipLevel;

public final class DefaultEligibility implements EligibilityPolicy {
    /** 
     * @param s
     * @param o
     * @return boolean
     */
    @Override
    public boolean canApply(Student s, InternshipOpportunity o) {
        if (s.getMajor() != o.getPreferredMajor()) return false;
        if (s.getYearOfStudy() <= 2) return o.getLevel() == InternshipLevel.BASIC;
        return true;
    }
}
