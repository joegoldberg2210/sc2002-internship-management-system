package entity;

import enumerations.*;

import java.io.Serializable;
import java.time.LocalDate;


public class InternshipOpportunity implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;                        
    private String title;                      
    private String description;               
    private InternshipLevel level;             
    private Major preferredMajor;             
    private LocalDate openDate;                     
    private LocalDate closeDate;                    
    private OpportunityStatus status;          
    private final String companyName;               
    private boolean visibility;                
    private int slots;                         
    private int confirmedSlots;               
    private CompanyRepresentative repInCharge; 

    public InternshipOpportunity(String id, String title, String description, InternshipLevel level, Major preferredMajor, LocalDate openDate, LocalDate closeDate, String companyName, int slots, CompanyRepresentative repInCharge) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.level = level;
        this.preferredMajor = preferredMajor; 
        this.openDate = openDate;
        this.closeDate = closeDate;
        this.companyName = companyName;
        this.slots = slots;
        this.repInCharge = repInCharge;

        this.status = OpportunityStatus.PENDING;
        this.visibility = false;
        this.confirmedSlots = 0;
    }

    /** 
     * @param s
     * @return boolean
     */

    public boolean isOpenFor(Student s) {
        if (s == null) return false;
        if (status != OpportunityStatus.APPROVED) return false;  
        if (!visibility) return false;                           
        var today = java.time.LocalDate.now();
        if (today.isBefore(openDate) || today.isAfter(closeDate)) return false;
        if (!hasVacancy()) return false;                        
        return isEligibleFor(s);                              
    }

    /** 
     * @param s
     * @return boolean
     */
    public boolean isEligibleFor(Student s) {
        if (s.getMajor() != preferredMajor) return false;

        if (s.getYearOfStudy() <= 2) return level == InternshipLevel.BASIC;
        return true;
    }

    public boolean hasVacancy() {
        return confirmedSlots < slots;
    }

    public boolean isEditable() {
        return status == OpportunityStatus.PENDING || status == OpportunityStatus.REJECTED;
    }

    public void incrementConfirmedSlots() {
        if (confirmedSlots < slots)
            confirmedSlots++;
    }

    public void resetConfirmedSlots() {
        this.confirmedSlots = 0;
    }

    /** 
     * @return String
     */
    public String getId() {
        return id;
    }

    /** 
     * @return String
     */
    public String getTitle() {
        return title;
    }

    /** 
     * @return String
     */
    public String getDescription() {
        return description;
    }

    /** 
     * @return InternshipLevel
     */
    public InternshipLevel getLevel() {
        return level;
    }

    /** 
     * @return Major
     */
    public Major getPreferredMajor() { 
        return preferredMajor; 
    }

    /** 
     * @return LocalDate
     */
    public LocalDate getOpenDate() {
        return openDate;
    }

    /** 
     * @return LocalDate
     */
    public LocalDate getCloseDate() {
        return closeDate;
    }

    /** 
     * @return OpportunityStatus
     */
    public OpportunityStatus getStatus() {
        return status;
    }

    /** 
     * @return String
     */
    public String getCompanyName() {
        return companyName;
    }

    /** 
     * @return boolean
     */
    public boolean isVisible() {
        return visibility;
    }

    /** 
     * @return int
     */
    public int getSlots() {
        return slots;
    }

    /** 
     * @return int
     */
    public int getConfirmedSlots() {
        return confirmedSlots;
    }

    /** 
     * @return CompanyRepresentative
     */
    public CompanyRepresentative getRepInCharge() {
        return repInCharge;
    }

    /** 
     * @param id
     */
    public void setId(String id) { 
        this.id = id; 
    }

    /** 
     * @param status
     */
    public void setStatus(OpportunityStatus status) {
        this.status = status;
    }

    /** 
     * @param visibility
     */
    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    /** 
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /** 
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
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
     * @param openDate
     */
    public void setOpenDate(LocalDate openDate) {
        this.openDate = openDate;
    }

    /** 
     * @param closeDate
     */
    public void setCloseDate(LocalDate closeDate) {
        this.closeDate = closeDate;
    }

    /** 
     * @param slots
     */
    public void setSlots(int slots) {
        this.slots = slots;
    }

    /** 
     * @param repInCharge
     */
    public void setRepInCharge(CompanyRepresentative repInCharge) {
        this.repInCharge = repInCharge;
    }

    /** 
     * @param confirmedSlots
     */
    public void setConfirmedSlots(int confirmedSlots) {
        this.confirmedSlots = confirmedSlots;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s) - %s | Slots: %d/%d | Status: %s",
                id, title, level, companyName, confirmedSlots, slots, status);
    }

}
