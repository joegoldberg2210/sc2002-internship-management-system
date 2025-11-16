package config;

import policy.EligibilityPolicy;
import policy.DefaultEligibility;

public final class DomainRules {
    private static EligibilityPolicy eligibility = new DefaultEligibility();

    private DomainRules() {}

    public static EligibilityPolicy eligibility() { return eligibility; }

    
    /** 
     * @param p
     */
    public static void setEligibility(EligibilityPolicy p) {
        if (p == null) throw new IllegalArgumentException("policy cannot be null");
        eligibility = p;
    }
}
