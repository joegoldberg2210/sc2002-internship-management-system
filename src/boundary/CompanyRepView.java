package boundary;

import java.util.List;
import java.util.Scanner;

import control.DataLoader;
import entity.CompanyRepresentative;
import entity.User;
import ui.ConsoleUI;

public class CompanyRepView {
    private final Scanner sc;
    private final CompanyRepresentative rep;
    private final List<User> users;
    private final DataLoader loader;

    public CompanyRepView(Scanner sc, CompanyRepresentative rep, List<User> users, DataLoader loader) {
        this.sc = sc;
        this.rep = rep;
        this.users = users;
        this.loader = loader;
    }

    public void run() {
        ConsoleUI.sectionHeader("Company Representative View");

        boolean running = true;
        while (running) {
            showMenu();
            System.out.print("Enter choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> manageAccount();
                case "2" -> manageOpportunities();
                case "3" -> reviewApplications();
                case "4" -> running = false;
                default -> System.out.println("✗ Invalid choice, please try again.\n");
            }
        }

        System.out.println();
        System.out.println("✓ You have logged out of the system.");
        System.out.println();
    }

    private void showMenu() {
        System.out.println("(1) Manage Account");
        System.out.println("(2) Manage Internship Opportunities");
        System.out.println("(3) Review Applications");
        System.out.println("(4) Logout");
        System.out.println();
    }

    private void manageAccount() {
        ConsoleUI.sectionHeader("Company Representative View > Manage Account");
        System.out.println("(1) View Profile");
        System.out.println("(2) Change Password");
        System.out.println("(0) Back to Company Representative View");
        System.out.println();
        System.out.print("Enter choice: ");
        String choice = sc.nextLine().trim();
        switch (choice) {
            case "1" -> viewProfile();
            case "2" -> changePassword();
            case "0" -> run();
        }
    }

    private void viewProfile() {
        ConsoleUI.sectionHeader("Company Representative View > Manage Account > View Profile");
        System.out.println("ID        : " + rep.getId());
        System.out.println("Name      : " + rep.getName());
        System.out.println("Company   : " + rep.getCompanyName());
        System.out.println("Department: " + rep.getDepartment());
        System.out.println("Position  : " + rep.getPosition());
        System.out.println("Status    : " + rep.getStatus());
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
        ConsoleUI.sectionHeader("Company Representative View > Manage Account > Change Password");

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

        boolean ok = rep.changePassword(current, newPwd);
        if (ok) {
            System.out.println("\n✓ Password changed successfully!");
            loader.saveUsers(users);
            manageAccount();
        } else {
            System.out.println("\n✗ Current password is incorrect.");
        }
    }

    private void manageOpportunities() {
        ConsoleUI.sectionHeader("Company Representative View > Manage Internship Opportunities");
        System.out.println();
    }

    private void reviewApplications() {
        ConsoleUI.sectionHeader("Company Representative View > Review Applications");
        System.out.println();
    }
}