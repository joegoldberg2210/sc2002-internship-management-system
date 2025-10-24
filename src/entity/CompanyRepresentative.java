package entity;

import enumerations.AccountStatus;

import java.util.Objects;

/** Company representative; must be approved before fully active. */
public class CompanyRepresentative extends User {
    private final String companyName;
    private final String department;
    private final String position;
    private AccountStatus status;         // PENDING, APPROVED, REJECTED
    private int activeListingsCount = 0;  // maintained by services (max 5 active)
    private UserPreferences preferences;  // 0..1

    public CompanyRepresentative(
            String id, String name, String password,
            String companyName, String department, String position,
            AccountStatus status
    ) {
        super(id, name, password);
        this.companyName = Objects.requireNonNull(companyName);
        this.department = Objects.requireNonNull(department);
        this.position = Objects.requireNonNull(position);
        this.status = Objects.requireNonNull(status);
    }

    public String getCompanyName() { return companyName; }
    public String getDepartment()   { return department; }
    public String getPosition()     { return position; }

    public AccountStatus getStatus()           { return status; }
    public void setStatus(AccountStatus status){ this.status = Objects.requireNonNull(status); }

    /** Business rule: at most 5 active listings at a time. */
    public boolean canCreateMore() { return activeListingsCount < 5; }

    /* package-private hooks for the service layer to maintain the count */
    void _incActive() { activeListingsCount++; }
    void _decActive() { activeListingsCount = Math.max(0, activeListingsCount - 1); }

    public UserPreferences getPreferences()              { return preferences; }
    public void setPreferences(UserPreferences prefs)    { this.preferences = prefs; }
}
