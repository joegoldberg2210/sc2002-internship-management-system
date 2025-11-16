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
     * @return String
     */
    public String getId() { 
        return id; 
    }

    /** 
     * @return Application
     */
    public Application getApplication() { 
        return application; 
    }

    /** 
     * @return Student
     */
    public Student getRequestedBy() { 
        return requestedBy; 
    }

    /** 
     * @return LocalDate
     */
    public LocalDate getRequestedAt() { 
        return requestedAt; 
    }

    /** 
     * @return WithdrawalStatus
     */
    public WithdrawalStatus getStatus() { 
        return status; 
    }
    
    /** 
     * @return CareerCenterStaff
     */
    public CareerCenterStaff getReviewedBy() { 
        return reviewedBy; 
    }

    /** 
     * @return LocalDate
     */
    public LocalDate getReviewedAt() { 
        return reviewedAt; 
    }

    /**
     *
     * @param application
     */
    public void setApplication(Application application) { 
        this.application = application; 
    }

    /** 
     * @param requestedBy
     */
    public void setRequestedBy(Student requestedBy) { 
        this.requestedBy = requestedBy; 
    }

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
