package entity;

import enumerations.*;
import java.util.Date;

/** Represents filtering criteria used to view internship opportunities or generate reports.*/
public class FilterCriteria {
    private final OpportunityStatus status;      // Filter by opportunity status
    private final String preferredMajor;         // Filter by applicant's preferred major
    private final InternshipLevel level;         // Filter by internship level
    private final Date closingDateBefore;        // Filter by closing date
    private final String company;                // Filter by company name

    public FilterCriteria(OpportunityStatus status, String preferredMajor,
                          InternshipLevel level, Date closingDateBefore, String company) {
        this.status = status;
        this.preferredMajor = preferredMajor;
        this.level = level;
        this.closingDateBefore = closingDateBefore;
        this.company = company;
    }

    // === Getters ===
    public OpportunityStatus getStatus() { return status; }
    public String getPreferredMajor() { return preferredMajor; }
    public InternshipLevel getLevel() { return level; }
    public Date getClosingDateBefore() { return closingDateBefore; }
    public String getCompany() { return company; }

    /**
     * Returns a readable string summary of this filter.
     */
    @Override
    public String toString() {
        return String.format("FilterCriteria[Status=%s, Major=%s, Level=%s, ClosingBefore=%s, Company=%s]",
                status, preferredMajor, level, closingDateBefore, company);
    }
}
