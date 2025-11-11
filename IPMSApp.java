import java.util.List;
import java.util.Scanner;

import boundary.LoginView;
import boundary.StudentView;
import boundary.CompanyRepView;
import boundary.CareerCenterStaffView;
import control.AuthControl;
import control.DataLoader;
import control.AccountApprovalService;
import control.ApplicationService;
import control.OpportunityService;
import entity.InternshipOpportunity;
import entity.Student;
import entity.CompanyRepresentative;
import entity.CareerCenterStaff;
import entity.User;
import entity.Application;

public class IPMSApp {
    public static void main(String[] args) {
        DataLoader loader = new DataLoader();

        // load users (includes pending/approved/rejected in one list)
        List<User> users = loader.loadUsers();

        // load opportunities (no csv fallback; empty list if none)
        List<InternshipOpportunity> opportunities = loader.loadOpportunities();
        List<Application> applications = loader.loadApplications();

        // pass loader so opportunityservice can persist after each change
        OpportunityService oppService = new OpportunityService(opportunities, loader);
        ApplicationService appService = new ApplicationService(applications, loader);

        if (users.isEmpty()) {
            System.out.println("no users loaded. please check your serialized/users.ser file.");
            return;
        } else {
            System.out.println(users.size() + " users loaded.");
        }

        System.out.println(opportunities.size() + " internship opportunities loaded.\n");

        try (Scanner sc = new Scanner(System.in)) {
            AuthControl auth = new AuthControl(users);
            AccountApprovalService approval = new AccountApprovalService(users, loader);

            // ─────────────────────────────────────────────────────────────
            // outer loop: after any view returns (student/rep/staff),
            // we come back here and show the login/create menu again.
            // the ONLY way to exit the app is for LoginView.run(...) to return null.
            // ─────────────────────────────────────────────────────────────
            while (true) {
                LoginView login = new LoginView(sc);
                User logged = login.run(auth, approval);

                // login view returns null only if user chose "exit system"
                if (logged == null) {
                    System.out.println("bye!");
                    break;
                }

                // dispatch into the correct view
                if (logged instanceof Student s) {
                    // inside StudentView, when the user chooses "logout",
                    // the view should just `return;` (not call run() again).
                    new StudentView(sc, s, users, loader, oppService, appService).run();
                } else if (logged instanceof CompanyRepresentative cr) {
                    new CompanyRepView(sc, cr, users, loader, oppService, appService).run();
                } else if (logged instanceof CareerCenterStaff staff) {
                    new CareerCenterStaffView(sc, staff, users, loader, approval, oppService).run();
                }

                // when any view returns (e.g., they set `running = false`), we land here
                // and immediately loop back to show the login/create menu again.

                // ensure session cleanup + persistence between sessions
                auth.logout(logged);
                loader.saveUsers(users);           // persist user edits (password, new CR registrations, approvals)
                loader.saveOpportunities(opportunities); // persist opportunity edits/approvals
                loader.saveApplications(applications);   // persist application submissions/decisions
            }
        }
    }
}