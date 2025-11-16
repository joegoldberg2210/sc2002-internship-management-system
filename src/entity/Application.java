package entity;

import enumerations.ApplicationStatus;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class Application implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final Student student;                   
    private final InternshipOpportunity opportunity; 
    private ApplicationStatus status;
    private final LocalDate appliedAt;
    private LocalDate decisionAt;
    private boolean accepted;

    public Application(String id, Student student, InternshipOpportunity opportunity) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.student = Objects.requireNonNull(student, "student must not be null");
        this.opportunity = Objects.requireNonNull(opportunity, "opportunity must not be null");
        this.appliedAt = LocalDate.now();
        this.status = ApplicationStatus.PENDING;
        this.accepted = false;
    }

    /** 
     * @param approve
     */
    public void markDecision(boolean approve) {
        this.status = approve ? ApplicationStatus.SUCCESSFUL : ApplicationStatus.UNSUCCESSFUL;
        this.decisionAt = LocalDate.now();
        if (!approve) this.accepted = false;
    }

    public void markAccepted() {
        if (this.status != ApplicationStatus.SUCCESSFUL)
            throw new IllegalStateException("cannot accept unless status is SUCCESSFUL.");
        this.accepted = true;
    }

    public void markWithdrawn() {
        this.status = ApplicationStatus.WITHDRAWN;
        this.accepted = false;
        this.decisionAt = LocalDate.now();
    }

    /** 
     * @return boolean
     */
    public boolean isActive() {
        return this.status == ApplicationStatus.PENDING;
    }

    /** 
     * @return String
     */

    public String getId() { 
        return id; 
    }
    /** 
     * @return Student
     */
    public Student getStudent() { 
        return student; 
    }
    /** 
     * @return InternshipOpportunity
     */
    public InternshipOpportunity getOpportunity() { 
        return opportunity; 
    }
    /** 
     * @return ApplicationStatus
     */
    public ApplicationStatus getStatus() { 
        return status; 
    }
    /** 
     * @return LocalDate
     */
    public LocalDate getAppliedAt() { 
        return appliedAt; 
    }
    /** 
     * @return LocalDate
     */
    public LocalDate getDecisionAt() { 
        return decisionAt; 
    }
    /** 
     * @return boolean
     */
    public boolean isAccepted() { 
        return accepted; 
    }

    /** 
     * @return String
     */
    @Override
    public String toString() {
        return String.format("application[%s] student=%s, opp=%s, status=%s",
                id, student.getId(), opportunity.getId(), status);
    }
}