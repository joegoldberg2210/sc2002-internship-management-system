import java.util.List;
import java.util.Scanner;

import boundary.LoginView;
import boundary.StudentView;
import boundary.CompanyRepView;
import boundary.CareerCenterStaffView;
import control.AuthControl;
import control.DataLoader;
import control.AccountApprovalService;
import control.OpportunityService;
import entity.InternshipOpportunity;
import entity.Student;
import entity.CompanyRepresentative;
import entity.CareerCenterStaff;
import entity.User;

public class IPMSApp {
    public static void main(String[] args) {
        DataLoader loader = new DataLoader();

        // load users (includes pending/approved/rejected in one list)
        List<User> users = loader.loadUsers();

        // load opportunities (no csv fallback; empty list if none)
        List<InternshipOpportunity> opportunities = loader.loadOpportunities();

        // pass loader so opportunityservice can persist after each change
        OpportunityService oppService = new OpportunityService(opportunities, loader);

        if (users.isEmpty()) {
            System.out.println("no users loaded. please check your serialized/users.ser or csv seed.");
            return;
        } else {
            System.out.println(users.size() + " users loaded.");
        }

        System.out.println(opportunities.size() + " internship opportunities loaded.\n");

        try (Scanner sc = new Scanner(System.in)) {
            AuthControl auth = new AuthControl(users);
            AccountApprovalService approval = new AccountApprovalService(users, loader);
            LoginView login = new LoginView(sc);

            User logged = login.run(auth, approval);

            if (logged instanceof Student s) {
                new StudentView(sc, s, users, loader).run();
            } else if (logged instanceof CompanyRepresentative cr) {
                new CompanyRepView(sc, cr, users, loader, oppService).run();
            } else if (logged instanceof CareerCenterStaff staff) {
                new CareerCenterStaffView(sc, staff, users, loader, approval).run();
            }

            auth.logout(logged);

            // persist user changes like password updates or new registrations
            loader.saveUsers(users);
        }
    }
}