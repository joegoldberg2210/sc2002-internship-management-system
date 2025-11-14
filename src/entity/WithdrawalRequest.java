package entity;

import enumerations.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

/**
 * Represents a student's request to withdraw from an internship application.
 * Each request records which application it belongs to, who requested it,
 * when it was made, and the staff who reviewed it.
 */
public class WithdrawalRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    // === Attributes (from UML) ===
    private final String id;                          // Unique identifier for the withdrawal request
    private Application application;            // The application that the student wishes to withdraw from
    private Student requestedBy;                // The student who made the withdrawal request
    private final LocalDate requestedAt;                   // Date the request was created
    private WithdrawalStatus status;              // Current withdrawal status (Pending, Approved, Rejected)
    private CareerCenterStaff reviewedBy;       // The staff member who reviewed the request
    private LocalDate reviewedAt;                    // Date when the request was reviewed

    // === Constructor ===
    public WithdrawalRequest(String id, Application application, Student requestedBy) {
        this.id = id;
        this.application = application;
        this.requestedBy = requestedBy;
        this.requestedAt = LocalDate.now();
        this.status = WithdrawalStatus.PENDING;   // Default state when created
    }

    // === Behavioural Methods ===

    /** Reviews the withdrawal request by a Career Center Staff. Updates the withdrawal status and review timestamp.*/
    public void review(CareerCenterStaff staff, boolean approve) {
        this.reviewedBy = staff;
        this.status = approve ? WithdrawalStatus.APPROVED : WithdrawalStatus.REJECTED;
        this.reviewedAt = LocalDate.now();
    }

    // === Utility Methods ===

    /** Checks whether the request has already been reviewed.*/
    public boolean isReviewed() {
        return status == WithdrawalStatus.APPROVED || status == WithdrawalStatus.REJECTED;
    }

    /** Resets the review details (e.g., if reassignment or re-evaluation is needed).*/
    public void resetReview() {
        this.reviewedBy = null;
        this.reviewedAt = null;
        this.status = WithdrawalStatus.PENDING;
    }

    // === Getters ===
    public String getId() { return id; }
    public Application getApplication() { return application; }
    public Student getRequestedBy() { return requestedBy; }
    public LocalDate getRequestedAt() { return requestedAt; }
    public WithdrawalStatus getStatus() { return status; }
    public CareerCenterStaff getReviewedBy() { return reviewedBy; }
    public LocalDate getReviewedAt() { return reviewedAt; }

    // === Setters (only for reversible attributes) ===
    public void setApplication(Application application) { this.application = application; }
    public void setRequestedBy(Student requestedBy) { this.requestedBy = requestedBy; }

    // === toString() for readable display ===
    @Override
    public String toString() {
        String result = String.format("WithdrawalRequest[%s] - Status: %s", id, status);
        if (isReviewed()) {
            result += String.format(" (Reviewed by %s on %s)",
                    reviewedBy != null ? reviewedBy.getName() : "Unknown",
                    reviewedAt != null ? reviewedAt : "N/A");
        }
        return result;
    }
}
