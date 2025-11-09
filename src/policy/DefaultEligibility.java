package policy;

import entity.InternshipOpportunity;
import entity.Student;
import enumerations.InternshipLevel;

public final class DefaultEligibility implements EligibilityPolicy {
    @Override
    public boolean canApply(Student s, InternshipOpportunity o) {
        // keep your current logic here
        if (s.getMajor() != o.getPreferredMajor()) return false;
        if (s.getYearOfStudy() <= 2) return o.getLevel() == InternshipLevel.BASIC;
        return true;
    }
}
