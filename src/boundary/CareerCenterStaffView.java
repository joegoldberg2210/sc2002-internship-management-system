package boundary;

import java.util.List;
import java.util.Scanner;

import control.AccountApprovalService;
import control.DataLoader;
import control.ApplicationService;
import control.OpportunityService;
import entity.CareerCenterStaff;
import entity.CompanyRepresentative;
import entity.InternshipOpportunity;
import entity.User;
import entity.WithdrawalRequest;
import ui.ConsoleUI;

public class CareerCenterStaffView {
    private final Scanner sc;
    private final CareerCenterStaff staff;
    private final List<User> users;
    private final DataLoader loader;
    private final AccountApprovalService approval;
    private final OpportunityService oppService;
    private final ApplicationService applicationService;

    public CareerCenterStaffView(Scanner sc, CareerCenterStaff staff, List<User> users, DataLoader loader, AccountApprovalService approval, OpportunityService oppService, ApplicationService applicationService) {
        this.sc = sc;
        this.staff = staff;
        this.users = users;
        this.loader = loader;
        this.approval = approval;
        this.oppService = oppService;
        this.applicationService = applicationService;
    }

    public void run() {
        ConsoleUI.sectionHeader("Career Center Staff View");

        boolean running = true;
        while (running) {
            showMenu();
            System.out.print("Enter choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> manageAccount();
                case "2" -> reviewPendingRegistrations();
                case "3" -> reviewPendingInternshipOpportunities();
                case "4" -> reviewPendingWithdrawalRequests();
                case "5" -> viewAllRegistrations();
                case "6" -> viewAllInternshipOpportunities();
                case "7" -> viewAllWithdrawalRequests();
                case "logout" -> {
                    System.out.println("\n✓ You have logged out of your account.\n");
                    return;
                }
                default -> System.out.println("✗ Invalid choice, please try again.\n");
            }
        }
 
        System.out.println();
        System.out.println("✓ You have logged out of the system.");
        System.out.println();
    }

    private void showMenu() {
        System.out.println("(1) Manage Account");
        System.out.println("(2) Review Pending Company Representative Registrations");
        System.out.println("(3) Review Pending Internship Opportunities");
        System.out.println("(4) Review Pending Withdrawal Requests");
        System.out.println("(5) View All Company Representative Registrations");   
        System.out.println("(6) View All Internship Opportunities");
        System.out.println("(7) View All Withdrawal Requests");
        System.out.println();
        System.out.println("→ Type 'logout' here to logout");
        System.out.println();
    }

    private void manageAccount() {
        ConsoleUI.sectionHeader("Career Center Staff View > Manage ccount");
        System.out.println("(1) View Profile");
        System.out.println("(2) Change Password");
        System.out.println("(0) Back to Career Center Staff View");
        System.out.println();
        System.out.print("Enter choice: ");
        String c = sc.nextLine().trim();
        switch (c) {
            case "1" -> viewProfile();
            case "2" -> changePassword();
            case "0" -> { ConsoleUI.sectionHeader("Career Center Staff View"); }
            default -> { ConsoleUI.sectionHeader("Student View"); }
        }
    }

    private void viewProfile() {
        ConsoleUI.sectionHeader("Career Center Staff View > Manage Account > View Profile");
        System.out.println("ID         : " + staff.getId());
        System.out.println("Name       : " + staff.getName());
        System.out.println("Department : " + staff.getStaffDepartment());
        System.out.println();
        System.out.println("(0) Back to Career Center Staff View to Manage Account");
        System.out.println();
        System.out.print("Enter choice: ");
        String choice = sc.nextLine().trim();
        switch (choice) {
            case "0" -> ConsoleUI.sectionHeader("Career Center Staff View > Manage Account");
        }
    }

    private void changePassword() {
        ConsoleUI.sectionHeader("Career Center Staff View > Manage Account > Change Password");

        System.out.print("Enter current password: ");
        String current = sc.nextLine().trim();

        System.out.print("Enter new password: ");
        String newPwd = sc.nextLine().trim();

        System.out.print("Confirm new password: ");
        String confirm = sc.nextLine().trim();

       boolean successful = newPwd.equals(confirm) && staff.changePassword(current, newPwd);

        if (successful) {
            System.out.println("\n✓ Password changed successfully!");
            loader.saveUsers(users);
            ConsoleUI.sectionHeader("Career Center Staff View");
        } else {
            System.out.println("\n✗ Unable to change password. Please try again.");
            ConsoleUI.sectionHeader("Career Center Staff View");
        }
    }

    private void reviewPendingRegistrations() {
        ConsoleUI.sectionHeader("Career Center Staff View > Review Pending Company Representative Registrations");

        List<CompanyRepresentative> pending = approval.getPendingCompanyReps();
        if (pending == null || pending.isEmpty()) {
            System.out.println("✗ No pending company representative registrations.\n");
            System.out.print("Press enter to continue... ");
            sc.nextLine();
            ConsoleUI.sectionHeader("Career Center Staff View");
            return;
        }

        // table header
        System.out.println();
        System.out.printf(
            "%-4s %-28s %-25s %-25s %-20s %-20s%n",
            "S/N", "Company Representative ID", "Name", "Company", "Department", "Position"
        );
        System.out.println("--------------------------------------------------------------------------------------------------------------------");

        int i = 1;
        for (CompanyRepresentative r : pending) {
            System.out.printf(
                "%-4d %-28s %-25s %-25s %-20s %-20s%n",
                i++,
                r.getId(),
                r.getName(),
                r.getCompanyName(),
                r.getDepartment(),
                r.getPosition()
            );
        }

        System.out.println("\n(Total: " + pending.size() + " pending company representative applications)\n");

        CompanyRepresentative selected = null;

        while (true) {
            System.out.print("Enter Company Representative ID to review (blank to cancel): ");
            String id = sc.nextLine().trim();

            if (id.isEmpty()) {
                System.out.println("Review cancelled.\n");
                ConsoleUI.sectionHeader("Career Center Staff View");
                return;
            }

            selected = pending.stream()
                    .filter(r -> r.getId().equalsIgnoreCase(id))
                    .findFirst()
                    .orElse(null);

            if (selected != null) break;

            System.out.println("✗ Invalid Company Representative ID. Please try again.\n");
        }

        // detail block
        System.out.println("\n────────────────────────────────────────────────────────────");
        System.out.println("          Company Representative Application Details        ");
        System.out.println("────────────────────────────────────────────────────────────");
        System.out.printf("%-25s: %s%n", "Name", selected.getName());
        System.out.printf("%-25s: %s%n", "Company Representative ID", selected.getId());
        System.out.printf("%-25s: %s%n", "Company", selected.getCompanyName());
        System.out.printf("%-25s: %s%n", "Department", selected.getDepartment());
        System.out.printf("%-25s: %s%n", "Position", selected.getPosition());
        System.out.println("────────────────────────────────────────────────────────────\n");

        // action menu
        System.out.print("\nApprove this company representative application? (y/n): ");
        String decision = sc.nextLine().trim().toLowerCase();

        boolean approve = decision.equals("y") || decision.equals("yes");
        if (approve) {
            approval.approveCompanyRep(staff, selected);
        } else {
            approval.rejectCompanyRep(staff, selected);
        }

        System.out.print("Press enter to return... ");
        sc.nextLine();
        ConsoleUI.sectionHeader("Career Center Staff View");
    }

    private void reviewPendingInternshipOpportunities() {
        ConsoleUI.sectionHeader("Career Center Staff View > Review Pending Internship Opportunities");

        List<InternshipOpportunity> pending = oppService.getPending();
        if (pending.isEmpty()) {
            System.out.println("✗ No pending internship opportunities.\n");
            ConsoleUI.sectionHeader("Career Center Staff View");
            return;
        }

        // table header
        System.out.println();
        System.out.printf(
            "%-4s %-15s %-25s %-20s %-20s %-15s %-20s %-12s %-16s%n",
            "S/N", "Opportunity ID", "Internship Title", "Preferred Major", "Internship Level", "Total Slots", "Company", "Open Date", "Closing Date"
        );
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------------------");

        int i = 1;
        for (InternshipOpportunity o : pending) {
            System.out.printf(
                "%-4d %-15s %-25s %-20s %-20s %-15s %-20s %-12s %-16s%n",
                i++,
                o.getId(),
                o.getTitle(),
                String.valueOf(o.getPreferredMajor()),
                String.valueOf(o.getLevel()),
                o.getSlots(),
                o.getCompanyName(),
                String.valueOf(o.getOpenDate()),
                String.valueOf(o.getCloseDate())
            );
        }

        System.out.println("\n(Total: " + pending.size() + " pending internship opportunities)\n");

        InternshipOpportunity selected = null;

        while (true) {
            System.out.println();
            System.out.print("Enter Opportunity ID to review (blank to cancel): ");
            String id = sc.nextLine().trim();

            if (id.isEmpty()) {
                System.out.println("Review cancelled.\n");
                ConsoleUI.sectionHeader("Career Center Staff View");
                return;
            }

            selected = pending.stream()
                    .filter(o -> o.getId().equalsIgnoreCase(id))
                    .findFirst()
                    .orElse(null);

            if (selected != null) break; // success → exit loop

            System.out.println("✗ Invalid Opportunity ID. Please try again.\n");
        }

        // detail view
        System.out.println();
        System.out.println("\n────────────────────────────────────────────────────────────");
        System.out.println("                 Internship Opportunity Details              ");
        System.out.println("────────────────────────────────────────────────────────────");
        System.out.printf("%-18s: %s%n", "Opportunity ID", selected.getId());
        System.out.printf("%-18s: %s%n", "Internship Title", selected.getTitle());
        System.out.printf("%-18s: %s%n", "Company", selected.getCompanyName());
        System.out.printf("%-18s: %s%n", "Preferred Major", selected.getPreferredMajor());
        System.out.printf("%-18s: %s%n", "Internship Level", selected.getLevel());
        System.out.printf("%-18s: %s%n", "Open Date", selected.getOpenDate());
        System.out.printf("%-18s: %s%n", "Close Date", selected.getCloseDate());
        System.out.printf("%-18s: %d/%d%n", "Slots", selected.getConfirmedSlots(), selected.getSlots());
        System.out.printf("%-18s: %s%n", "Status", selected.getStatus());
        System.out.println("────────────────────────────────────────────────────────────\n");

        System.out.print("\nApprove this internship opportunity? (y/n): ");
        String decision = sc.nextLine().trim().toLowerCase();

        boolean approve = decision.equals("y") || decision.equals("yes");
        if (approve) {
            oppService.approveOpportunity(staff, selected);
        } else {
            oppService.rejectOpportunity(staff, selected);
        }

        System.out.println();
        System.out.print("Press enter to return... ");
        sc.nextLine();
        ConsoleUI.sectionHeader("Career Center Staff View"); 
    }

    private void viewAllRegistrations() {
        ConsoleUI.sectionHeader("Career Center Staff View > View All Company Representative Registrations");

        // get all company representatives
        List<CompanyRepresentative> allReps = approval.getAllCompanyReps();

        if (allReps == null || allReps.isEmpty()) {
            System.out.println("✗ No company representative registrations found.\n");
            System.out.print("Press enter to continue... ");
            sc.nextLine();
            ConsoleUI.sectionHeader("Career Center Staff View");
            return;
        }

        // table header
        System.out.println();
        System.out.printf(
            "%-4s %-40s %-25s %-25s %-25s %-25s %-15s%n",
            "S/N", "Company Representative ID", "Name", "Company", "Department", "Position", "Status"
        );
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------");

        int i = 1;
        for (CompanyRepresentative r : allReps) {
            System.out.printf(
                "%-4d %-40s %-25s %-25s %-25s %-25s %-15s%n",
                i++,
                r.getId(),
                r.getName(),
                r.getCompanyName(),
                r.getDepartment(),
                r.getPosition(),
                r.getStatus()
            );
        }

        System.out.println("\n(Total: " + allReps.size() + " company representative registrations)\n");

        System.out.print("Press enter to return... ");
        sc.nextLine();
        ConsoleUI.sectionHeader("Career Center Staff View");
    }

    /** view all pending withdrawal requests and approve/reject them */
    private void reviewPendingWithdrawalRequests() {
        ConsoleUI.sectionHeader("Career Center Staff > View Pending Withdrawal Requests");

        List<WithdrawalRequest> pending = applicationService.getPendingWithdrawalRequests();

        if (pending.isEmpty()) {
            System.out.println("✓ No pending withdrawal requests.\n");
            System.out.print("Press enter to return... "); sc.nextLine();
            return;
        }

        System.out.printf("%-4s %-10s %-15s %-15s %-15s %-20s %-15s %-20s%n",
                "S/N", "Request ID", "Application ID", "Student ID", "Opportunity ID", "Internship Title", "Company", "Requested At");
        System.out.println("---------------------------------------------------------------------------------------------------------------------------");

        int i = 1;
        for (WithdrawalRequest req : pending) {
            System.out.printf("%-4d %-10s %-15s %-15s %-15s %-20s %-15s %-20s%n",
                    i++,
                    req.getId(),
                    req.getApplication().getId(),
                    req.getRequestedBy().getId(),
                    req.getApplication().getOpportunity().getId(),
                    req.getApplication().getOpportunity().getTitle(),
                    req.getApplication().getOpportunity().getCompanyName(),
                    req.getRequestedAt());
        }

        System.out.println("\n(Total: " + pending.size() + " pending withdrawal requests)\n");

        WithdrawalRequest selected = null;

        while (true) {
            System.out.println();
            System.out.print("Enter Request ID to review (blank to cancel): ");
            String id = sc.nextLine().trim();

            if (id.isEmpty()) {
                System.out.println("Request cancelled.\n");
                ConsoleUI.sectionHeader("Career Center Staff View");
                return;
            }

            selected = pending.stream()
                    .filter(r -> r.getId().equalsIgnoreCase(id))
                    .findFirst()
                    .orElse(null);

            if (selected != null) break;

            System.out.println("✗ Invalid Request ID. Please try again.\n");
        }

        // print request details
        System.out.println("\n────────────────────────────────────────────────────────────");
        System.out.println("                Withdrawal Request Details                  ");
        System.out.println("────────────────────────────────────────────────────────────");
        System.out.printf("%-18s: %s%n", "Request ID", selected.getId());
        System.out.printf("%-18s: %s%n", "Student ID", selected.getRequestedBy().getId());
        System.out.printf("%-18s: %s%n", "Application ID", selected.getApplication().getId());
        System.out.printf("%-18s: %s%n", "Internship Title", selected.getApplication().getOpportunity().getTitle());
        System.out.printf("%-18s: %s%n", "Internship Level", selected.getApplication().getOpportunity().getLevel());
        System.out.printf("%-18s: %s%n", "Company", selected.getApplication().getOpportunity().getCompanyName());
        System.out.printf("%-18s: %s%n", "Preferred Major", selected.getApplication().getOpportunity().getPreferredMajor());
        System.out.printf("%-18s: %s%n", "Requested At", selected.getRequestedAt());
        System.out.println("────────────────────────────────────────────────────────────");

        System.out.print("\nApprove this withdrawal request? (y/n): ");
        String decision = sc.nextLine().trim().toLowerCase();

        boolean approve = decision.equals("y") || decision.equals("yes");
        applicationService.reviewWithdrawalRequest(staff, selected, approve);

        System.out.println();
        System.out.print("Press enter to return... "); sc.nextLine();
        ConsoleUI.sectionHeader("Career Center Staff View");
    }

    private void viewAllInternshipOpportunities() {
        ConsoleUI.sectionHeader("Career Center Staff View > View All Internship Opportunities");

        List<InternshipOpportunity> all = oppService.getAllOpportunities();
        if (all == null || all.isEmpty()) {
            System.out.println("✗ No internship opportunities found.\n");
            System.out.print("Press enter to return... ");
            sc.nextLine();
            ConsoleUI.sectionHeader("Career Center Staff View");
            return;
        }

        // table header
        System.out.println();
        System.out.printf(
            "%-4s %-15s %-25s %-20s %-20s %-20s %-20s %-12s %-16s %-16s%n",
            "S/N", "Opportunity ID", "Internship Title", "Preferred Major", "Internship Level", "Available Slots", "Company", "Open Date", "Closing Date", "Status"
        );
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

        int i = 1;
        for (InternshipOpportunity o : all) {
            String slotsStr = String.format("%d/%d", o.getConfirmedSlots(), o.getSlots());
            System.out.printf(
                "%-4d %-15s %-25s %-20s %-20s %-20s %-20s %-12s %-16s %-16s%n",
                i++,
                o.getId(),
                o.getTitle(),
                String.valueOf(o.getPreferredMajor()),
                String.valueOf(o.getLevel()),
                slotsStr,
                o.getCompanyName(),
                String.valueOf(o.getOpenDate()),
                String.valueOf(o.getCloseDate()),
                o.getStatus()
            );
        }

        System.out.println("\n(Total: " + all.size() + " internship opportunities)\n");

        System.out.print("Press enter to return... ");
        sc.nextLine();
        ConsoleUI.sectionHeader("Career Center Staff View");
    }

    /** view all withdrawal requests (read-only) */
    private void viewAllWithdrawalRequests() {
        ConsoleUI.sectionHeader("Career Center Staff > View All Withdrawal Requests");

        // get all (pending + approved + rejected, depending on implementation)
        List<WithdrawalRequest> all = applicationService.getAllWithdrawalRequests();

        if (all == null || all.isEmpty()) {
            System.out.println("✓ No withdrawal requests found.\n");
            System.out.print("Press enter to return... ");
            sc.nextLine();
            return;
        }

        // table header
        System.out.printf("%-4s %-10s %-15s %-15s %-15s %-20s %-15s %-15s %-20s%n",
                "S/N", "Request ID", "Application ID", "Student ID", "Opportunity ID", "Internship Title", "Company", "Status", "Requested At");
        System.out.println("----------------------------------------------------------------------------------------------------------------------------------------");

        int i = 1;
        for (WithdrawalRequest req : all) {
            System.out.printf("%-4d %-10s %-15s %-15s %-15s %-20s %-15s %-15s %-20s%n",
                    i++,
                    req.getId(),
                    req.getApplication().getId(),
                    req.getRequestedBy().getId(),
                    req.getApplication().getOpportunity().getId(),
                    req.getApplication().getOpportunity().getTitle(),
                    req.getApplication().getOpportunity().getCompanyName(),
                    req.getStatus(),
                    req.getRequestedAt());
        }

        System.out.println("\n(Total: " + all.size() + " withdrawal requests)\n");
        System.out.println();
        System.out.print("Press enter to return... ");
        sc.nextLine();
        ConsoleUI.sectionHeader("Career Center Staff View");
    }
}