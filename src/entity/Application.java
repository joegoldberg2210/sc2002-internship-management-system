package entity;

import enumerations.ApplicationStatus;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/** represents one student's application to one internship opportunity. */
public class Application implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final Student student;                   // linked student
    private final InternshipOpportunity opportunity; // linked internship
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

    /** company rep approves or rejects an application. */
    public void markDecision(boolean approve) {
        this.status = approve ? ApplicationStatus.SUCCESSFUL : ApplicationStatus.UNSUCCESSFUL;
        this.decisionAt = LocalDate.now();
        if (!approve) this.accepted = false;
    }

    /** student accepts a successful offer. */
    public void markAccepted() {
        if (this.status != ApplicationStatus.SUCCESSFUL)
            throw new IllegalStateException("cannot accept unless status is SUCCESSFUL.");
        this.accepted = true;
    }

    /** student withdraws their application. */
    public void markWithdrawn() {
        this.status = ApplicationStatus.WITHDRAWN;
        this.accepted = false;
        this.decisionAt = LocalDate.now();
    }

    /** true if this application is still pending review. */
    public boolean isActive() {
        return this.status == ApplicationStatus.PENDING;
    }

    // --- getters ---

    public String getId() { 
        return id; 
    }
    public Student getStudent() { 
        return student; 
    }
    public InternshipOpportunity getOpportunity() { 
        return opportunity; 
    }
    public ApplicationStatus getStatus() { 
        return status; 
    }
    public LocalDate getAppliedAt() { 
        return appliedAt; 
    }
    public LocalDate getDecisionAt() { 
        return decisionAt; 
    }
    public boolean isAccepted() { 
        return accepted; 
    }

    // optional for easier debugging
    @Override
    public String toString() {
        return String.format("application[%s] student=%s, opp=%s, status=%s",
                id, student.getId(), opportunity.getId(), status);
    }
}