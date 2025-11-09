package policy;

import entity.Student;
import entity.InternshipOpportunity;

public interface EligibilityPolicy {
    boolean canApply(Student s, InternshipOpportunity o);
}
