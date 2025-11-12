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
                case "2" -> reviewRegistrations();
                case "3" -> reviewInternshipOpportunities();
                case "4" -> reviewWithdrawalRequests();
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
        System.out.println("(2) Review Company Representative Registrations");
        System.out.println("(3) Review Internship Opportunities");
        System.out.println("(4) Review Withdrawal Requests");
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
        }
    }

    private void viewProfile() {
        ConsoleUI.sectionHeader("Career Center Staff View > Manage Account > View Profile");
        System.out.println("ID         : " + staff.getId());
        System.out.println("Name       : " + staff.getName());
        System.out.println("Department : " + staff.getStaffDepartment());
        System.out.println();
        System.out.println("(0) Back to Manage Account");
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

    private void reviewRegistrations() {
        ConsoleUI.sectionHeader("Career Center Staff View > Review Company Representative Registrations");

        List<CompanyRepresentative> pending = approval.getPendingCompanyReps();
        if (pending == null || pending.isEmpty()) {
            System.out.println("✗ No pending company representative registration(s).\n");
            System.out.print("Press enter to continue... ");
            sc.nextLine();
            ConsoleUI.sectionHeader("Career Center Staff View");
            return;
        }

        // table header
        System.out.println();
        System.out.printf(
            "%-4s %-15s %-25s %-25s %-20s %-20s%n",
            "S/N", "ID", "Name", "Company", "Department", "Position"
        );
        System.out.println("---------------------------------------------------------------------------------------------------------------");

        int i = 1;
        for (CompanyRepresentative r : pending) {
            System.out.printf(
                "%-4d %-15s %-25s %-25s %-20s %-20s%n",
                i++,
                r.getId(),
                r.getName(),
                r.getCompanyName(),
                r.getDepartment(),
                r.getPosition()
            );
        }

        System.out.println("\n(Total: " + pending.size() + " pending company representative applications)\n");

        // selection
        System.out.print("Select # to review (or 0 to go back): ");
        int idx = readIndex(pending.size());
        if (idx == 0) {
            ConsoleUI.sectionHeader("Career Center Staff View");
            return;
        }

        CompanyRepresentative rep = pending.get(idx - 1);

        // detail block (clean, aligned)
        System.out.println("\n────────────────────────────────────────────────────────────");
        System.out.println("          Company Representative Application Details        ");
        System.out.println("────────────────────────────────────────────────────────────");
        System.out.printf("%-18s: %s%n", "Name", rep.getName());
        System.out.printf("%-18s: %s%n", "Representative ID", rep.getId());
        System.out.printf("%-18s: %s%n", "Company", rep.getCompanyName());
        System.out.printf("%-18s: %s%n", "Department", rep.getDepartment());
        System.out.printf("%-18s: %s%n", "Position", rep.getPosition());
        System.out.println("────────────────────────────────────────────────────────────\n");

        // action menu
        System.out.println("(1) Approve Application");
        System.out.println("(2) Reject Application");
        System.out.println("(0) Back to Career Center Staff View");
        System.out.println();  
        System.out.print("Enter choice: ");
        String c = sc.nextLine().trim();

        switch (c) {
            case "1" -> {
                boolean ok = approval.approveCompanyRep(staff, rep);
                System.out.println(ok ? "✓ Approved.\n" : "✗ Could not approve (duplicate id or not found in pending).\n");
                System.out.print("Press enter to continue... ");
                sc.nextLine();
                ConsoleUI.sectionHeader("Career Center Staff View");
            }
            case "2" -> {
                boolean ok = approval.rejectCompanyRep(staff, rep);
                System.out.println(ok ? "✓ Rejected.\n" : "✗ Could not reject (not found in pending).\n");
                System.out.print("Press enter to continue... ");
                sc.nextLine();
                ConsoleUI.sectionHeader("Career Center Staff View");
            }
            case "0" -> {
                ConsoleUI.sectionHeader("Career Center Staff View");
            }
            default -> System.out.println("✗ Invalid choice.\n");
        }
    }

    private void reviewInternshipOpportunities() {
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
            "%-4s %-15s %-25s %-12s %-12s %-9s %-15s %-12s %-16s%n",
            "S/N", "ID", "Title", "Major", "Level", "Slots", "Company", "Open Date", "Closing Date"
        );
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------");

        int i = 1;
        for (InternshipOpportunity o : pending) {
            String slotsStr = String.format("%d/%d", o.getConfirmedSlots(), o.getSlots());
            System.out.printf(
                "%-4d %-15s %-25s %-12s %-12s %-9s %-15s %-12s %-16s%n",
                i++,
                o.getId(),
                o.getTitle(),
                String.valueOf(o.getPreferredMajor()),
                String.valueOf(o.getLevel()),
                slotsStr,
                o.getCompanyName(),
                String.valueOf(o.getOpenDate()),
                String.valueOf(o.getCloseDate())
            );
        }

        System.out.println("\n(Total: " + pending.size() + " pending internship opportunities)\n");

        // selection
        System.out.print("Select # to review (or 0 to go back): ");
        int idx = readIndex(pending.size());
        if (idx == 0) {
            ConsoleUI.sectionHeader("Career Center Staff View");
            return;
        }

        InternshipOpportunity sel = pending.get(idx - 1);

        // detail view
        System.out.println();
        System.out.println("\n────────────────────────────────────────────────────────────");
        System.out.println("                 Internship Opportunity Details              ");
        System.out.println("────────────────────────────────────────────────────────────");
        System.out.printf("%-18s: %s%n", "Opportunity ID", sel.getId());
        System.out.printf("%-18s: %s%n", "Internship Title", sel.getTitle());
        System.out.printf("%-18s: %s%n", "Company", sel.getCompanyName());
        System.out.printf("%-18s: %s%n", "Major", sel.getPreferredMajor());
        System.out.printf("%-18s: %s%n", "Level", sel.getLevel());
        System.out.printf("%-18s: %s%n", "Open Date", sel.getOpenDate());
        System.out.printf("%-18s: %s%n", "Close Date", sel.getCloseDate());
        System.out.printf("%-18s: %d/%d%n", "Slots", sel.getConfirmedSlots(), sel.getSlots());
        System.out.printf("%-18s: %s%n", "Status", sel.getStatus());
        System.out.println("────────────────────────────────────────────────────────────\n");
        System.out.println();

        System.out.println("(1) Approve");
        System.out.println("(2) Reject");
        System.out.println("(0) Back to Career Center Staff View");
        System.out.println();
        System.out.print("Enter choice: ");
        String c = sc.nextLine().trim();

        switch (c) {
            case "1" -> {
                oppService.approveOpportunity(staff, sel); // persists inside service
                System.out.println("✓ Approved. Opportunity is now visible to eligible students.\n");
                System.out.print("Press enter to continue... ");
                sc.nextLine();
                ConsoleUI.sectionHeader("Career Center Staff View");
            }
            case "2" -> {
                oppService.rejectOpportunity(staff, sel);  // persists inside service
                System.out.println("✓ Rejected.\n");
                System.out.print("Press enter to continue... ");
                sc.nextLine();
                ConsoleUI.sectionHeader("Career Center Staff View");
            }
            case "0" -> {
                ConsoleUI.sectionHeader("Career Center Staff View");
            }
            default -> System.out.println("✗ Invalid choice.\n");
        }
    }

    private void printRepList(String title, List<CompanyRepresentative> list) {
        System.out.println(title + ":");
        int i = 1;
        for (CompanyRepresentative r : list) {
            System.out.printf("(%d) %s | %s | %s | status: %s%n", i++, r.getName(), r.getCompanyName(), r.getId(), r.getStatus());
        }
        System.out.println();
    }

    private int readIndex(int max) {
        while (true) {
            String s = sc.nextLine().trim();
            if (s.matches("\\d+")) {
                int v = Integer.parseInt(s);
                if (v >= 0 && v <= max) return v;
            }
            System.out.print("Enter 0-" + max + ": ");
        }
    }

     /** view all pending withdrawal requests and approve/reject them */
    private void reviewWithdrawalRequests() {
        ConsoleUI.sectionHeader("career center staff > pending withdrawal requests");

        List<WithdrawalRequest> pending = applicationService.getPendingWithdrawalRequests();

        if (pending.isEmpty()) {
            System.out.println("✓ no pending withdrawal requests.\n");
            System.out.print("press enter to return... "); sc.nextLine();
            return;
        }

        System.out.printf("%-4s %-10s %-15s %-15s %-20s %-15s %-15s %-20s%n",
                "s/n", "request id", "app id", "student id", "internship title", "company", "status", "requested at");
        System.out.println("--------------------------------------------------------------------------------------------------------------------");

        int i = 1;
        for (WithdrawalRequest req : pending) {
            System.out.printf("%-4d %-10s %-15s %-15s %-20s %-15s %-15s %-20s%n",
                    i++,
                    req.getId(),
                    req.getApplication().getId(),
                    req.getRequestedBy().getId(),
                    req.getApplication().getOpportunity().getTitle(),
                    req.getApplication().getOpportunity().getCompanyName(),
                    req.getStatus(),
                    req.getRequestedAt());
        }

        System.out.println();
        System.out.print("Enter request ID to review (blank to cancel): ");
        String id = sc.nextLine().trim();
        if (id.isEmpty()) {
            System.out.println("Request cancelled.\n");
            ConsoleUI.sectionHeader("Career Center Staff View");
            return;
        }

        WithdrawalRequest selected = pending.stream()
                .filter(r -> r.getId().equalsIgnoreCase(id))
                .findFirst().orElse(null);

        if (selected == null) {
            System.out.println("✗ Invalid Request ID. Please check and try again.\n");
            System.out.println();
            System.out.print("Press enter to return... "); 
            sc.nextLine();
            ConsoleUI.sectionHeader("Career Center Staff View");
            return;
        }

        // print request details
        System.out.println("\n────────────────────────────────────────────────────────────");
        System.out.println("                WITHDRAWAL REQUEST DETAILS                  ");
        System.out.println("────────────────────────────────────────────────────────────");
        System.out.printf("%-18s: %s%n", "Request ID", selected.getId());
        System.out.printf("%-18s: %s%n", "Student ID", selected.getRequestedBy().getId());
        System.out.printf("%-18s: %s%n", "Application ID", selected.getApplication().getId());
        System.out.printf("%-18s: %s%n", "Internship Title", selected.getApplication().getOpportunity().getTitle());
        System.out.printf("%-18s: %s%n", "Internship Level", selected.getApplication().getOpportunity().getLevel());
        System.out.printf("%-18s: %s%n", "Company", selected.getApplication().getOpportunity().getCompanyName());
        System.out.printf("%-18s: %s%n", "Preferred Major", selected.getApplication().getOpportunity().getPreferredMajor());
        System.out.printf("%-18s: %s%n", "Status", selected.getStatus());
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
}