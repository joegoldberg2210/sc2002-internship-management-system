package boundary;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import control.AccountApprovalService;
import control.DataLoader;
import entity.CareerCenterStaff;
import entity.CompanyRepresentative;
import entity.User;
import enumerations.AccountStatus;
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
                case "3" -> running = false;
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
        System.out.println("(3) Logout");
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

        List<CompanyRepresentative> pending = getCompanyRepsByStatus(AccountStatus.PENDING);
        if (pending.isEmpty()) {
            System.out.println("No pending registration(s).\n");
            return;
        }

        printRepList("Pending Registrations", pending);

        System.out.print("Select # to review (or 0 to go back): ");
        int idx = readIndex(pending.size());
        if (idx == 0) return;

        CompanyRepresentative rep = pending.get(idx - 1);
        System.out.println();
        System.out.println("Selected. : " + rep.getName() + " (" + rep.getId() + ")");
        System.out.println("Company   : " + rep.getCompanyName());
        System.out.println("Department: " + rep.getDepartment());
        System.out.println("Position  : " + rep.getPosition());
        System.out.println();

        System.out.println("(1) Approve");
        System.out.println("(2) Reject");
        System.out.println("(0) Back");
        System.out.print("Enter choice: ");
        String c = sc.nextLine().trim();

        switch (c) {
            case "1" -> {
                approval.approveCompanyRep(staff, rep);
                loader.saveUsers(users);
                System.out.println("✓ approved.\n");
            }
            case "2" -> {
                System.out.print("Enter rejection reason: ");
                String reason = sc.nextLine().trim();
                approval.rejectCompanyRep(staff, rep, reason);
                loader.saveUsers(users);
                System.out.println("✓ rejected.\n");
            }
            case "0" -> { /* back */ }
            default -> System.out.println("✗ Invalid choice.\n");
        }
    }

    private List<CompanyRepresentative> getCompanyRepsByStatus(AccountStatus status) {
        List<CompanyRepresentative> list = new ArrayList<>();
        for (User u : users) {
            if (u instanceof CompanyRepresentative cr && cr.getStatus() == status) list.add(cr);
        }
        return list;
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