package control;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import entity.CareerCenterStaff;
import entity.CompanyRepresentative;
import entity.User;
import enumerations.AccountStatus;

public class AccountApprovalService {
    private final List<User> users;  
    private final DataLoader loader;

    public AccountApprovalService(List<User> users, DataLoader loader) {
        this.users  = Objects.requireNonNull(users, "users must not be null");
        this.loader = Objects.requireNonNull(loader, "loader must not be null");
    }

    /** 
     * @param rep
     * @return boolean
     */
    public boolean submitCompanyRepRegistration(CompanyRepresentative rep) {
        if (rep == null) return false;

        final String key = User.canonical(rep.getId());

        boolean duplicate = users.stream()
                .anyMatch(u -> User.canonical(u.getId()).equals(key));
        if (duplicate) return false;

        rep.setStatus(AccountStatus.PENDING);
        users.add(rep);
        loader.saveUsers(users);
        return true;
    }

    /** 
     * @param staff
     * @param rep
     * @return boolean
     */
    public boolean approveCompanyRep(CareerCenterStaff staff, CompanyRepresentative rep) {
        if (staff == null || rep == null) return false;

        CompanyRepresentative stored = findRep(rep.getId());
        if (stored == null) return false;

        stored.setStatus(AccountStatus.APPROVED);
        loader.saveUsers(users);
        return true;
    }

    /** 
     * @param staff
     * @param rep
     * @return boolean
     */
    public boolean rejectCompanyRep(CareerCenterStaff staff, CompanyRepresentative rep) {
        if (staff == null || rep == null) return false;

        CompanyRepresentative stored = findRep(rep.getId());
        if (stored == null) return false;

        stored.setStatus(AccountStatus.REJECTED);
        loader.saveUsers(users);
        return true;
    }

    /** 
     * @param id
     * @return CompanyRepresentative
     */
    private CompanyRepresentative findRep(String id) {
        final String key = User.canonical(id);
        for (User u : users) {
            if (u instanceof CompanyRepresentative cr
                    && User.canonical(cr.getId()).equals(key)) {
                return cr;
            }
        }
        return null;
    }

    /** 
     * @return List<CompanyRepresentative>
     */
    public List<CompanyRepresentative> getPendingCompanyReps() {
        List<CompanyRepresentative> pending = new ArrayList<>();
        for (User u : users) {
            if (u instanceof CompanyRepresentative cr && cr.getStatus() == AccountStatus.PENDING) {
                pending.add(cr);
            }
        }
        return pending;
    }

    /** 
     * @return List<CompanyRepresentative>
     */
    public List<CompanyRepresentative> getAllCompanyReps() {
        List<CompanyRepresentative> all = new ArrayList<>();
        for (User u : users) {
            if (u instanceof CompanyRepresentative cr) {
                all.add(cr);
            }
        }
        return all;
    }
}