package entity;

import java.io.Serializable;
import java.util.Date;

import enumerations.InternshipLevel;
import enumerations.OpportunityStatus;

public class FilterCriteria implements Serializable {

    private OpportunityStatus status;     // pending / approved / filled / rejected
    private String preferredMajor;        // stored as string name
    private InternshipLevel level;        // basic / intermediate / advanced
    private String company;               // optional for search
    private Date closingDateBefore;       // optional

    public FilterCriteria() {
        // empty constructor required
    }

    // === getters ===
    public OpportunityStatus getStatus() { return status; }
    public String getPreferredMajor() { return preferredMajor; }
    public InternshipLevel getLevel() { return level; }
    public String getCompany() { return company; }
    public Date getClosingDateBefore() { return closingDateBefore; }

    // === setters ===
    public void setStatus(OpportunityStatus status) {
        this.status = status;
    }

    public void setPreferredMajor(String preferredMajor) {
        this.preferredMajor = preferredMajor;
    }

    public void setLevel(InternshipLevel level) {
        this.level = level;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setClosingDateBefore(Date date) {
        this.closingDateBefore = date;
    }
}