package entity;

import enumerations.AccountStatus;

import java.util.Objects;

public class CompanyRepresentative extends User {
    private final String companyName;
    private final String department;
    private final String position;
    private AccountStatus status;        
    private int activeListingsCount;

    public CompanyRepresentative(String id, String name, String companyName, String department, String position, AccountStatus status) {
        super(id, name);
        this.companyName = Objects.requireNonNull(companyName);
        this.department = Objects.requireNonNull(department);
        this.position = Objects.requireNonNull(position);
        this.status = Objects.requireNonNull(status);
    }

    /** 
     * @return String
     */
    public String getCompanyName() { 
        return companyName; 
    
    }
    /** 
     * @return String
     */
    public String getDepartment() { 
        return department; 
    }

    /** 
     * @return String
     */
    public String getPosition() { 
        return position; 
    }

    /** 
     * @return AccountStatus
     */
    public AccountStatus getStatus() { 
        return status; 
    }

    /** 
     * @param status
     */
    public void setStatus(AccountStatus status) { 
        this.status = Objects.requireNonNull(status); 
    }

    public void incrementListings() { 
        activeListingsCount++; 
    }

    public void decrementListings() { 
        if (activeListingsCount > 0) activeListingsCount--; 
    }
}
