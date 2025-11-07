package boundary;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import control.AccountApprovalService;
import control.DataLoader;
import entity.CareerCenterStaff;
import entity.CompanyRepresentative;
import entity.InternshipOpportunity;
import entity.User;
import enumerations.AccountStatus;
import enumerations.OpportunityStatus;
import ui.ConsoleUI;

public class CareerCenterStaffView {
    private final Scanner sc;
    private final CareerCenterStaff staff;
    private final List<User> users;
    private final DataLoader loader;
    private final AccountApprovalService approval;

    public CareerCenterStaffView(Scanner sc, CareerCenterStaff staff, List<User> users, DataLoader loader, AccountApprovalService approval) {
        this.sc = sc;
        this.staff = staff;
        this.users = users;
        this.loader = loader;
        this.approval = approval;
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
                case "0" -> running = false;
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
        System.out.println("(0) Logout");
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
            case "0" -> run();
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
            case "0" -> manageAccount();
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

        if (!newPwd.equals(confirm)) {
            System.out.println("\n✗ Passwords do not match.");
            return;
        }

        boolean ok = staff.changePassword(current, newPwd);
        if (ok) {
            System.out.println("\n✓ Password changed successfully!");
            loader.saveUsers(users);
            manageAccount();
        } else {
            System.out.println("\n✗ Current password is incorrect.");
        }
    }

    private void reviewRegistrations() {
        ConsoleUI.sectionHeader("Career Center Staff View > Review Company Representative Registrations");

        List<CompanyRepresentative> pending = approval.getPendingCompanyReps();
        if (pending == null || pending.isEmpty()) {
            System.out.println("no pending registration(s).\n");
            return;
        }

        printRepList("Pending Registrations", pending);

        System.out.print("select # to review (or 0 to go back): ");
        int idx = readIndex(pending.size());
        if (idx == 0) return;

        CompanyRepresentative rep = pending.get(idx - 1);
        System.out.println();
        System.out.println("selected : " + rep.getName() + " (" + rep.getId() + ")");
        System.out.println("company  : " + rep.getCompanyName());
        System.out.println("department: " + rep.getDepartment());
        System.out.println("position : " + rep.getPosition());
        System.out.println();

        System.out.println("(1) approve");
        System.out.println("(2) reject");
        System.out.println("(0) back");
        System.out.print("enter choice: ");
        String c = sc.nextLine().trim();

        switch (c) {
            case "1" -> {
                boolean ok = approval.approveCompanyRep(staff, rep);
                System.out.println(ok ? "✓ approved.\n"
                                    : "✗ could not approve (duplicate id or not found in pending).\n");
            }
            case "2" -> {
                // no rejection reason
                boolean ok = approval.rejectCompanyRep(staff, rep);
                // if you changed the service signature to rejectCompanyRep(staff, rep), then call that instead.
                System.out.println(ok ? "✓ rejected.\n"
                                    : "✗ could not reject (not found in pending).\n");
            }
            case "0" -> { /* back */ }
            default -> System.out.println("✗ invalid choice.\n");
        }
    }

    private void reviewInternshipOpportunities() {
        ConsoleUI.sectionHeader("Career Center Staff View > Review Pending Internship Opportunities");

        List<InternshipOpportunity> all = loader.loadOpportunities();
        List<InternshipOpportunity> pending = new ArrayList<>();
        for (InternshipOpportunity o : all) {
            if (o.getStatus() == OpportunityStatus.PENDING) pending.add(o);
        }

        if (pending.isEmpty()) {
            System.out.println("no pending internship opportunity.\n");
            return;
        }

        System.out.println("pending opportunities:");
        for (int i = 0; i < pending.size(); i++) {
            InternshipOpportunity o = pending.get(i);
            System.out.printf("(%d) %s [%s] — %s, level=%s, slots %d/%d%n",
                    i + 1, o.getTitle(), o.getCompanyName(), o.getPreferredMajor(),
                    o.getLevel(), o.getConfirmedSlots(), o.getSlots());
        }
        System.out.println();

        System.out.print("select # to review (or 0 to go back): ");
        int idx = readIndex(pending.size());
        if (idx == 0) return;

        InternshipOpportunity sel = pending.get(idx - 1);

        System.out.println();
        System.out.println("selected : " + sel.getTitle() + " (" + sel.getId() + ")");
        System.out.println("company  : " + sel.getCompanyName());
        System.out.println("major    : " + sel.getPreferredMajor());
        System.out.println("level    : " + sel.getLevel());
        System.out.println("open     : " + sel.getOpenDate());
        System.out.println("close    : " + sel.getCloseDate());
        System.out.println("slots    : " + sel.getConfirmedSlots() + "/" + sel.getSlots());
        System.out.println("status   : " + sel.getStatus());
        System.out.println();

        System.out.println("(1) approve");
        System.out.println("(2) reject");
        System.out.println("(0) back");
        System.out.print("enter choice: ");
        String c = sc.nextLine().trim();

        switch (c) {
            case "1" -> {
                sel.setStatus(OpportunityStatus.APPROVED);
                sel.setVisibility(true);
                loader.saveOpportunities(all);
                System.out.println("✓ approved. opportunity is now visible to eligible students.\n");
            }
            case "2" -> {
                // no rejection reason
                sel.setStatus(OpportunityStatus.REJECTED);
                sel.setVisibility(false);
                loader.saveOpportunities(all);
                System.out.println("✓ rejected.\n");
            }
            case "0" -> { /* back */ }
            default -> System.out.println("✗ invalid choice.\n");
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
}