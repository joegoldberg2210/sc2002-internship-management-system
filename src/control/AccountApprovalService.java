package control;

import java.util.List;

import entity.CareerCenterStaff;
import entity.CompanyRepresentative;
import entity.User;
import enumerations.AccountStatus;

public class AccountApprovalService {
    private final List<User> users;
    private final DataLoader loader;

    public AccountApprovalService(List<User> users, DataLoader loader) {
        this.users = users;
        this.loader = loader;
    }

    // submit a new company rep for approval
    public void submitCompanyRepRegistration(CompanyRepresentative rep) {
        if (rep == null) return;
        rep.setStatus(AccountStatus.PENDING);
        if (users.stream().noneMatch(u -> u.getId().equalsIgnoreCase(rep.getId()))) {
            users.add(rep);
        }
        loader.saveUsers(users);
    }

    // approve a pending company rep
    public void approveCompanyRep(CareerCenterStaff staff, CompanyRepresentative rep) {
        if (staff == null || rep == null) return;
        rep.setStatus(AccountStatus.APPROVED);
        loader.saveUsers(users);
    }

    // reject a pending company rep (reason stored on the rep if you have a field for it)
    public void rejectCompanyRep(CareerCenterStaff staff, CompanyRepresentative rep, String reason) {
        if (staff == null || rep == null) return;
        rep.setStatus(AccountStatus.REJECTED);
        loader.saveUsers(users);
    }
}