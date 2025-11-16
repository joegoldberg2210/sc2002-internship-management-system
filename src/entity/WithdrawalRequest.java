package entity;

import enumerations.*;

import java.io.Serializable;
import java.time.LocalDate;

public class WithdrawalRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id;                          
    private Application application;            
    private Student requestedBy;                
    private final LocalDate requestedAt;                   
    private WithdrawalStatus status;              
    private CareerCenterStaff reviewedBy;       
    private LocalDate reviewedAt;                    

    public WithdrawalRequest(String id, Application application, Student requestedBy) {
        this.id = id;
        this.application = application;
        this.requestedBy = requestedBy;
        this.requestedAt = LocalDate.now();
        this.status = WithdrawalStatus.PENDING;   
    }

    /** 
     * @param staff
     * @param approve
     */
    public void review(CareerCenterStaff staff, boolean approve) {
        this.reviewedBy = staff;
        this.status = approve ? WithdrawalStatus.APPROVED : WithdrawalStatus.REJECTED;
        this.reviewedAt = LocalDate.now();
    }

    /** 
     * @return boolean
     */
    public boolean isReviewed() {
        return status == WithdrawalStatus.APPROVED || status == WithdrawalStatus.REJECTED;
    }

    public void resetReview() {
        this.reviewedBy = null;
        this.reviewedAt = null;
        this.status = WithdrawalStatus.PENDING;
    }

    /** 
     * @param toString(
     * @return String
     */
    public String getId() { return id; }
    public Application getApplication() { return application; }
    public Student getRequestedBy() { return requestedBy; }
    public LocalDate getRequestedAt() { return requestedAt; }
    public WithdrawalStatus getStatus() { return status; }
    public CareerCenterStaff getReviewedBy() { return reviewedBy; }
    public LocalDate getReviewedAt() { return reviewedAt; }

    /** 
     * @param toString(
     */
    public void setApplication(Application application) { this.application = application; }
    public void setRequestedBy(Student requestedBy) { this.requestedBy = requestedBy; }

    /** 
     * @return String
     */
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
