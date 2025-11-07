package boundary;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import control.ApplicationService;
import control.DataLoader;
import control.OpportunityService;
import entity.InternshipOpportunity;
import entity.Student;
import entity.User;
import ui.ConsoleUI;

public class StudentView {
    private final Scanner sc;
    private final Student student;
    private final List<User> users;
    private final DataLoader loader;
    private final OpportunityService opportunityService;
    private final ApplicationService applicationService;

    public StudentView(Scanner sc, Student student, List<User> users, DataLoader loader, OpportunityService opportunityService, ApplicationService applicationService) {
        this.sc = sc;
        this.student = student;
        this.users = users;
        this.loader = loader;
        this.opportunityService = opportunityService;
        this.applicationService = applicationService;
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
                case "2" -> viewAvailableInternships();
                case "3" -> applyForInternship();
                case "4" -> viewApplications();
                case "5" -> withdrawApplication();
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
        System.out.println("(2) View Available Internships");
        System.out.println("(3) Apply New Internship");
        System.out.println("(4) View My Internship Applications");
        System.out.println("(5) Withdraw Internship Application");
        System.out.println("(0) Logout");
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

    private void viewAvailableInternships() {
        ConsoleUI.sectionHeader("Student View > Available Internship Opportunities");

        List<InternshipOpportunity> available = opportunityService.getAllOpportunities()
            .stream()
            .filter(o -> o.isOpenFor(student))
            .collect(Collectors.toList());

        if (available.isEmpty()) {
            System.out.println("✗ no internship opportunities currently available for your profile.\n");
            return;
        }

        int idx = 1;
        for (InternshipOpportunity o : available) {
            System.out.println("------------------------------------------------------------");
            System.out.printf("(%d) id           : %s%n", idx++, o.getId());
            System.out.println("     title        : " + o.getTitle());
            System.out.println("     company      : " + o.getCompanyName());
            System.out.println("     description  : " + o.getDescription());
            System.out.println("     preferred major : " + o.getPreferredMajor());
            System.out.println("     level        : " + o.getLevel());
            System.out.println("     open date    : " + o.getOpenDate());
            System.out.println("     close date   : " + o.getCloseDate());
            System.out.printf("     slots        : %d/%d%n", o.getConfirmedSlots(), o.getSlots());
            System.out.println("     status       : " + o.getStatus());
            System.out.println("     visibility   : " + (o.isVisible() ? "visible" : "hidden"));
            System.out.println("------------------------------------------------------------\n");
        }

        System.out.println();
    }

    private void viewApplications() {
        ConsoleUI.sectionHeader("Student View > View Internship Applications");
    }

    private void applyForInternship() {
        ConsoleUI.sectionHeader("Student View > Apply New Internship");
    }

    private void withdrawApplication() {
        ConsoleUI.sectionHeader("Student View > Withdraw Internship Application");
    }
}