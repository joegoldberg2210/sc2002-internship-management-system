package entity;

import java.io.Serializable;
import java.util.Date;

import enumerations.InternshipLevel;
import enumerations.OpportunityStatus;
import enumerations.Major;

public class FilterCriteria implements Serializable {

    private OpportunityStatus status;     
    private Major preferredMajor;         
    private InternshipLevel level;        
    private String company;               
    private Date closingDateBefore;       

    public FilterCriteria() {
    }

    /** 
     * @return OpportunityStatus
     */
    public OpportunityStatus getStatus() { 
        return status; 
    }

    /** 
     * @return Major
     */
    public Major getPreferredMajor() { 
        return preferredMajor; 
    }

    /** 
     * @return InternshipLevel
     */
    public InternshipLevel getLevel() { 
        return level; 
    }

    /** 
     * @return String
     */
    public String getCompany() { 
        return company; 
    }

    /** 
     * @return Date
     */
    public Date getClosingDateBefore() { 
        return closingDateBefore; 
    }

    /** 
     * @param status
     */
    // === setters ===
    public void setStatus(OpportunityStatus status) {
        this.status = status;
    }

    /** 
     * @param preferredMajor
     */
    public void setPreferredMajor(Major preferredMajor) {
        this.preferredMajor = preferredMajor;
    }

    /** 
     * @param level
     */
    public void setLevel(InternshipLevel level) {
        this.level = level;
    }

    /** 
     * @param company
     */
    public void setCompany(String company) {
        this.company = company;
    }

    /** 
     * @param date
     */
    public void setClosingDateBefore(Date date) {
        this.closingDateBefore = date;
    }
}