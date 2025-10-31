import java.util.List;
import java.util.Scanner;

import boundary.LoginView;
import boundary.StudentView;
import boundary.CompanyRepView;
import boundary.CareerCenterStaffView;
import control.AuthControl;
import control.DataLoader;
import control.AccountApprovalService;  // new
import control.OpportunityService;
import entity.InternshipOpportunity;
import entity.Student;
import entity.CompanyRepresentative;
import entity.CareerCenterStaff;
import entity.User;

public class IPMSApp {
    public static void main(String[] args) {
        DataLoader loader = new DataLoader();
        List<User> users = loader.loadUsers();
        List<InternshipOpportunity> opportunities = loader.loadOpportunities();
        OpportunityService oppService = new OpportunityService(opportunities);


        if (users.isEmpty()) {
            System.out.println("No users loaded. Please check CSV files.");
            return;
        } else {
            System.out.println(users.size() + " users loaded.\n");
            for (User u : users) {
                System.out.printf("id: %-12s | name: %-20s | role: %-25s%n",
                        u.getId(),
                        u.getName(),
                        u.getClass().getSimpleName());
            }
        }

        try (Scanner sc = new Scanner(System.in)) {
            AuthControl auth = new AuthControl(users);
            AccountApprovalService approval = new AccountApprovalService(users, loader); // new
            LoginView login = new LoginView(sc);

            // note: run(...) now takes approval too (for company-rep registration)
            User logged = login.run(auth, approval);

            if (logged instanceof Student s) {
                new StudentView(sc, s, users, loader).run();
            }
            else if (logged instanceof CompanyRepresentative cr) {
                new CompanyRepView(sc, cr, users, loader, oppService).run();
            }
            else if (logged instanceof CareerCenterStaff staff) {
                new CareerCenterStaffView(sc, staff, users, loader, approval).run();
            }

            auth.logout(logged);
            loader.saveUsers(users); // persist any changes (e.g., new registrations, password updates)
        }
    }
}
