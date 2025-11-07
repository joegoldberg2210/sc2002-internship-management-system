package entity;

import enumerations.AccountStatus;

import java.util.Objects;

/** Company representative; must be approved before fully active. */
public class CompanyRepresentative extends User {
    private final String companyName;
    private final String department;
    private final String position;
    private AccountStatus status;         // PENDING, APPROVED, REJECTED
    private int activeListingsCount;
    private UserPreferences preferences;  // 0..1

    public CompanyRepresentative(String id, String name, String companyName, String department, String position, AccountStatus status) {
        super(id, name);
        this.companyName = Objects.requireNonNull(companyName);
        this.department = Objects.requireNonNull(department);
        this.position = Objects.requireNonNull(position);
        this.status = Objects.requireNonNull(status);
    }

    public String getCompanyName() { 
        return companyName; 
    
    }
    public String getDepartment() { 
        return department; 
    }

    public String getPosition() { 
        return position; 
    }

    public AccountStatus getStatus() { 
        return status; 
    }

    public void setStatus(AccountStatus status) { 
        this.status = Objects.requireNonNull(status); 
    }

    // update it when new opportunity is added or deleted
    public void incrementListings() { 
        activeListingsCount++; 
    }

    public void decrementListings() { 
        if (activeListingsCount > 0) activeListingsCount--; 
    }

    public UserPreferences getPreferences() { 
        return preferences; 
    }
    public void setPreferences(UserPreferences prefs) { 
        this.preferences = prefs; 
    }
}
