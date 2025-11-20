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
import entity.WithdrawalRequest;
import entity.Application;

public class IPMSApp {
    public static void main(String[] args) {
        DataLoader loader = new DataLoader();

        List<User> users = loader.loadUsers();

        List<InternshipOpportunity> opportunities = loader.loadOpportunities();
        List<Application> applications = loader.loadApplications();
        List<WithdrawalRequest> withdrawals = loader.loadWithdrawalRequests();

        OpportunityService oppService = new OpportunityService(opportunities, loader);
        ApplicationService appService = new ApplicationService(applications, opportunities, withdrawals, loader);

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

            while (true) {
                LoginView login = new LoginView(sc);
                User logged = login.run(auth, approval);

                if (logged == null) {
                    System.out.println("bye!");
                    break;
                }

                if (logged instanceof Student s) {
                    new StudentView(sc, s, users, loader, oppService, appService).run();
                } else if (logged instanceof CompanyRepresentative cr) {
                    new CompanyRepView(sc, cr, users, loader, oppService, appService).run();
                } else if (logged instanceof CareerCenterStaff staff) {
                    new CareerCenterStaffView(sc, staff, users, loader, approval, oppService, appService).run();
                }

                auth.logout(logged);
                loader.saveUsers(users);           
                loader.saveOpportunities(opportunities); 
                loader.saveApplications(applications);   
                loader.saveWithdrawalRequests(withdrawals);
            }
        }
    }
}