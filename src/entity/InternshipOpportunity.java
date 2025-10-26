package entity;

import enumerations.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Represents an internship opportunity created by a Company Representative.
 * Includes details such as title, description, level, and status as well as operational methods to check if it is open or editable.
 */
public class InternshipOpportunity {

    private final String id;                         // Unique ID of the internship
    private String title;                      // Internship title
    private String description;                // Brief description of the internship
    private InternshipLevel level;             // Internship difficulty level (Basic, Intermediate, Advanced)
    private String preferredMajor;             // Preferred major of applicants
    private LocalDate openDate;                     // Opening date for applications
    private LocalDate closeDate;                    // Closing date for applications
    private OpportunityStatus status;          // Current status (Pending, Approved, Rejected, Filled)
    private final String companyName;                // Name of the company offering the internship
    private boolean visibility;                // Determines if internship is visible to students
    private int slots;                         // Maximum number of available positions
    private int confirmedSlots;                // Number of slots already filled
    private CompanyRepresentative repInCharge; // Company Representative managing this opportunity


    // === Constructor ===
    public InternshipOpportunity(String id, String title, String description, InternshipLevel level,
                                 String preferredMajor, LocalDate openDate, LocalDate closeDate,
                                 String companyName, int slots, CompanyRepresentative repInCharge) {
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

        // Default values on creation
        this.status = OpportunityStatus.PENDING;
        this.visibility = false;
        this.confirmedSlots = 0;
    }

    // === Operational Methods ===

    /**
     * Checks whether this internship is open for a given student at a particular date.
     */
    public boolean isOpenFor(Student student) {
        LocalDate today = LocalDate.now();
        return visibility
                && status == OpportunityStatus.APPROVED
                && (!today.isBefore(openDate))      // today >= openDate
                && (!today.isAfter(closeDate))      // today <= closeDate
                && hasVacancy()
                && isEligibleFor(student);
    }

    /* Eligibility: major must match: y1-2 only BASIC; y3+ any level. */
    public boolean isEligibleFor(Student s) {
        if (s == null) return false;
        if (!Objects.equals(preferredMajor, s.getMajor())) return false;
        if (s.getYearOfStudy() <= 2) return level == InternshipLevel.BASIC;
        return true;
    }


    /**
     * Checks if there are still available slots for students.
     */
    public boolean hasVacancy() {
        return confirmedSlots < slots;
    }

    /**
     * Determines if the internship can be edited by the representative.
     */
    public boolean isEditable() {
        return status == OpportunityStatus.PENDING || status == OpportunityStatus.REJECTED;
    }

    /**
     * Updates the confirmed slot count when a student is accepted.
     */
    public void incrementConfirmedSlots() {
        if (confirmedSlots < slots)
            confirmedSlots++;
    }

    /**
     * Resets the confirmed slots (e.g., when an opportunity is reopened).
     */
    public void resetConfirmedSlots() {
        this.confirmedSlots = 0;
    }

    // === Getters and Setters ===

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public InternshipLevel getLevel() {
        return level;
    }

    public String getPreferredMajor() {
        return preferredMajor;
    }

    public LocalDate getOpenDate() {
        return openDate;
    }

    public LocalDate getCloseDate() {
        return closeDate;
    }

    public OpportunityStatus getStatus() {
        return status;
    }

    public String getCompanyName() {
        return companyName;
    }

    public boolean isVisible() {
        return visibility;
    }

    public int getSlots() {
        return slots;
    }

    public int getConfirmedSlots() {
        return confirmedSlots;
    }

    public CompanyRepresentative getRepInCharge() {
        return repInCharge;
    }

    public void setStatus(OpportunityStatus status) {
        this.status = status;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPreferredMajor(String preferredMajor) {
        this.preferredMajor = preferredMajor;
    }

    public void setLevel(InternshipLevel level) {
        this.level = level;
    }

    public void setOpenDate(LocalDate openDate) {
        this.openDate = openDate;
    }

    public void setCloseDate(LocalDate closeDate) {
        this.closeDate = closeDate;
    }

    public void setSlots(int slots) {
        this.slots = slots;
    }

    public void setRepInCharge(CompanyRepresentative repInCharge) {
        this.repInCharge = repInCharge;
    }

    // === toString() ===

    /**
     * Provides a human-readable summary of the internship opportunity.
     */
    @Override
    public String toString() {
        return String.format("[%s] %s (%s) - %s | Slots: %d/%d | Status: %s",
                id, title, level, companyName, confirmedSlots, slots, status);
    }

}
