package entity;

import enumerations.ApplicationStatus;

import java.time.LocalDate;
import java.util.Objects;

/** Represents one student's application to one internship opportunity. */
public class Application {
    private final String id;
    private final Student student;                    // 1
    private final InternshipOpportunity opportunity;  // 1
    private ApplicationStatus status = ApplicationStatus.PENDING;
    private final LocalDate appliedAt;
    private LocalDate decisionAt;                     // null until decided
    private boolean accepted = false;                 // student-confirmed placement?

    public Application(String id, Student student, InternshipOpportunity opportunity) {
        this.id = Objects.requireNonNull(id);
        this.student = Objects.requireNonNull(student);
        this.opportunity = Objects.requireNonNull(opportunity);
        this.appliedAt = LocalDate.now();
    }

    // --- Core behaviors (per diagram) ---

    /** Company rep approves/rejects. Sets decision date and status. */
    public void markDecision(boolean approve) {
        this.status = approve ? ApplicationStatus.SUCCESSFUL
                : ApplicationStatus.UNSUCCESSFUL;
        this.decisionAt = LocalDate.now();
        if (!approve) {
            this.accepted = false; // ensure accepted=false on rejection
        }
    }

    /** Student accepts a SUCCESSFUL offer (one placement). */
    public void markAccepted() {
        if (this.status != ApplicationStatus.SUCCESSFUL) {
            throw new IllegalStateException("Cannot accept unless status is SUCCESSFUL.");
        }
        this.accepted = true;
    }

    /** Service may set to WITHDRAWN when needed (e.g., auto-withdraw others). */
    public void markWithdrawn() {
        this.status = ApplicationStatus.WITHDRAWN;
        this.accepted = false;
        this.decisionAt = LocalDate.now();
    }

    // --- Helpers often used by services/entities ---

    /** Active = still pending review. */
    public boolean isActive() {
        return this.status == ApplicationStatus.PENDING;
    }

    // --- Getters ---

    public String getId() { return id; }
    public Student getStudent() { return student; }
    public InternshipOpportunity getOpportunity() { return opportunity; }
    public ApplicationStatus getStatus() { return status; }
    public LocalDate getAppliedAt() { return appliedAt; }
    public LocalDate getDecisionAt() { return decisionAt; }
    public boolean isAccepted() { return accepted; }
}
