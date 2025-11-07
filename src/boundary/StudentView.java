package boundary;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import control.ApplicationService;
import control.DataLoader;
import control.OpportunityService;
import entity.InternshipOpportunity;
import entity.Student;
import entity.Application;
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
                case "logout" -> {
                    System.out.println("\n✓ You have logged out of your account.\n");
                    return;
                }
                default -> System.out.println("✗ Invalid choice, please try again.\n");
            }
        }
    }

    private void showMenu() {
        System.out.println("(1) Manage Account");
        System.out.println("(2) View Available Internships");
        System.out.println("(3) Apply New Internship");
        System.out.println("(4) View My Internship Applications");
        System.out.println("(5) Withdraw Internship Application");
        System.out.println();
        System.out.println("→ Type 'logout' here to logout");
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
            case "0" -> { ConsoleUI.sectionHeader("Student View"); }
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
            case "0" -> ConsoleUI.sectionHeader("Company Representative View > Manage Account");
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

        boolean successful = newPwd.equals(confirm) && student.changePassword(current, newPwd);

        if (successful) {
            System.out.println("\n✓ Password changed successfully!");
            loader.saveUsers(users);
            ConsoleUI.sectionHeader("Student View");
        } else {
            System.out.println("\n✗ Unable to change password. Please try again.");
            ConsoleUI.sectionHeader("Student View");
        }
    }

    private void viewAvailableInternships() {
        ConsoleUI.sectionHeader("Student View > View Available Internships");

        List<InternshipOpportunity> available = opportunityService.getAllOpportunities()
            .stream()
            .filter(o -> o.isOpenFor(student))
            .collect(Collectors.toList());

        if (available.isEmpty()) {
            System.out.println("✗ No internship opportunities currently available for your profile.\n");
            ConsoleUI.sectionHeader("Student View");
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
 
        ConsoleUI.sectionHeader("Student View");
    }

    private void viewApplications() {
        ConsoleUI.sectionHeader("Student View > View Internship Applications");

        List<Application> myApps = applicationService.getApplicationsForStudent(student);
        if (myApps.isEmpty()) {
            System.out.println("No applications yet.");
            ConsoleUI.sectionHeader("Student View");
        }

        for (Application a : myApps) {
            InternshipOpportunity o = a.getOpportunity();
            System.out.println("----------------------------------------");
            System.out.println("Application ID     : " + a.getId());
            System.out.println("Status             : " + a.getStatus());
            System.out.println("Applied at         : " + a.getAppliedAt());
            System.out.println("Offer at           : " + a.getDecisionAt());
            System.out.println("Accepted           : " + (a.isAccepted() ? "yes" : "no"));
            System.out.println("Opportunity        : " + o.getTitle() + " @ " + o.getCompanyName());
        }
 
        ConsoleUI.sectionHeader("Student View");
    }

    private void applyForInternship() {
        ConsoleUI.sectionHeader("Student View > Apply New Internship");
        
        // get all available internships open to this student
        List<InternshipOpportunity> available = opportunityService.getAllOpportunities().stream()
                .filter(o -> o.isOpenFor(student))
                .toList();

        if (available.isEmpty()) {
            System.out.println("✗ No internship opportunities currently available for you.\n");
            ConsoleUI.sectionHeader("Student View");
        }

        // display list
        System.out.println("Available internships:");
        for (int i = 0; i < available.size(); i++) {
            InternshipOpportunity o = available.get(i);
            System.out.printf("(%d) %s | %s | major=%s | level=%s | close=%s%n",
                    i + 1,
                    o.getCompanyName(),
                    o.getTitle(),
                    o.getPreferredMajor(),
                    o.getLevel(),
                    o.getCloseDate());
        }
        System.out.println();

        // let student choose
        System.out.print("Enter the number of the internship to apply: ");
        int choice = -1;
        while (choice < 0 || choice > available.size()) {
            try {
                choice = Integer.parseInt(sc.nextLine().trim());
                if (choice < 1 || choice > available.size()) {
                    System.out.print("Enter a valid number (1-" + available.size() + "): ");
                }
            } catch (NumberFormatException e) {
                System.out.print("Enter a valid number (1-" + available.size() + "): ");
            }
        }

        InternshipOpportunity selected = available.get(choice - 1);

        // confirm application
        System.out.printf("Apply for '%s' at %s? (y/n): ", selected.getTitle(), selected.getCompanyName());
        String confirm = sc.nextLine().trim().toLowerCase();

        if (confirm.equals("y") || confirm.equals("yes")) {
            Application result = applicationService.applyForOpportunity(student, selected);

            if (result != null) {
                System.out.println("✓ Application submitted successfully – " + result.getId());
            } else {
                long active = applicationService.getActiveCountForStudent(student.getId());
                boolean duplicate = applicationService.hasActiveApplication(student, selected);

                if (duplicate) {
                    System.out.println("✗ You already have an active application for this opportunity.\n");
                } else if (active >= 3) {
                    System.out.println("✗ You have reached the maximum of 3 active applications.\n");
                }
            }
        } else {
            System.out.println("Application cancelled.\n");
        }

 
        ConsoleUI.sectionHeader("Student View");
    }

    private void withdrawApplication() {
        ConsoleUI.sectionHeader("Student View > Withdraw Internship Application");
    }
}