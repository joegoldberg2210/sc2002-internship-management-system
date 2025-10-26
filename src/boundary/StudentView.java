package boundary;

import java.util.List;
import java.util.Scanner;

import control.DataLoader;
import entity.Student;
import entity.User;
import ui.ConsoleUI;

public class StudentView {
    private final Scanner sc;
    private final Student student;
    private final List<User> users;
    private final DataLoader loader;

    public StudentView(Scanner sc, Student student, List<User> users, DataLoader loader) {
        this.sc = sc;
        this.student = student;
        this.users = users;
        this.loader = loader;
    }

    public void run() {
        ConsoleUI.sectionHeader("Student View");

        boolean running = true;
        while (running) {
            showMenu();
            System.out.print("Enter choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> manageAccount();
                case "2" -> viewApplications();
                case "3" -> applyForInternship();
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
        System.out.println("(2) View Internship Applications");
        System.out.println("(3) Apply New Internship");
        System.out.println("(4) Logout");
        System.out.println();
    }

    private void manageAccount() {
        ConsoleUI.sectionHeader("Student View > Manage Account");
        System.out.println("(1) View Profile");
        System.out.println("(2) Change Password");
        System.out.println("(0) Back to Student View");
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
        ConsoleUI.sectionHeader("Student View > Manage Account > View Profile");
        System.out.println("ID    : " + student.getId());
        System.out.println("Name  : " + student.getName());
        System.out.println("Year  : " + student.getYearOfStudy());
        System.out.println("Major : " + student.getMajor());
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
        ConsoleUI.sectionHeader("Student View > Manage Account > Change Password");

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

        boolean successful = student.changePassword(current, newPwd);
        if (successful) {
            System.out.println("\n✓ Password changed successfully!");
            loader.saveUsers(users);
            manageAccount();
        } else {
            System.out.println("\n✗ Current password is incorrect.");
        }
    }

    private void viewApplications() {
        ConsoleUI.sectionHeader("Student View > View Internship Applications");
    }

    private void applyForInternship() {
        ConsoleUI.sectionHeader("Student View > Apply New Internship");
    }
}