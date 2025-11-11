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
                case "5" -> manageInternshipOffers();
                case "6" -> withdrawApplication();
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
        System.out.println("(5) Manage Internship Offers");
        System.out.println("(6) Withdraw Internship Application");
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
        System.out.print("Press enter key to continue... ");
        sc.nextLine();

        ConsoleUI.sectionHeader("Student View");
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

        System.out.println();
        System.out.printf(
            "%-4s %-10s %-25s %-20s %-15s %-15s %-20s%n",
            "S/N", "ID", "Title", "Company", "Major", "Level", "Available Slots"
        );
        System.out.println("--------------------------------------------------------------------------------------------------------------------------");

        int i = 1;
        for (InternshipOpportunity o : available) {
            String slotsStr = String.format("%d/%d", o.getConfirmedSlots(), o.getSlots());

            System.out.printf(
                "%-4d %-10s %-25s %-20s %-15s %-15s %-20s%n",
                i++,
                o.getId(),
                o.getTitle(),
                o.getCompanyName(),
                String.valueOf(o.getPreferredMajor()),
                String.valueOf(o.getLevel()),
                slotsStr
            );
        }

        System.out.println("\n(Total: " + available.size() + " internship opportunities available)");
        System.out.println();

        System.out.print("Press enter key to continue... ");
        sc.nextLine();

        ConsoleUI.sectionHeader("Student View");
    }

    private void viewApplications() {
        ConsoleUI.sectionHeader("Student View > View My Internship Applications");

        List<Application> myApps = applicationService.getApplicationsForStudent(student);

        if (myApps.isEmpty()) {
            System.out.println("✗ No internship applications found.\n");
            System.out.print("Press enter to return... ");
            sc.nextLine();
            ConsoleUI.sectionHeader("Student View");
            return;
        }

        System.out.println();
        System.out.printf(
            "%-4s %-15s %-25s %-28s %-20s %-15s %-12s %-12s%n",
            "S/N", "Application ID", "Internship Title", "Company", "Preferred Major", "Level", "Status", "Applied Date"
        );
        System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------");

        int i = 1;
        for (Application app : myApps) {
            InternshipOpportunity opp = app.getOpportunity();
            System.out.printf(
                "%-4d %-15s %-25s %-28s %-20s %-15s %-12s %-12s%n",
                i++,
                app.getId(),
                opp.getTitle(),
                opp.getCompanyName(),
                String.valueOf(opp.getPreferredMajor()),
                String.valueOf(opp.getLevel()),
                String.valueOf(app.getStatus()),
                String.valueOf(app.getAppliedAt())
            );
        }

        System.out.print("Press enter to return... ");
        sc.nextLine();
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
        System.out.println();
        System.out.printf(
            "%-4s %-20s %-25s %-20s %-20s %-15s %-20s%n",
            "S/N", "Opportunity ID", "Title", "Company", "Preferred Major", "Level", "Available Slots"
        );
        System.out.println("--------------------------------------------------------------------------------------------------------------------------");

        int i = 1;
        for (InternshipOpportunity o : available) {
            String slotsStr = String.format("%d/%d", o.getConfirmedSlots(), o.getSlots());

            System.out.printf(
                "%-4d %-20s %-25s %-20s %-20s %-15s %-20s%n",
                i++,
                o.getId(),
                o.getTitle(),
                o.getCompanyName(),
                String.valueOf(o.getPreferredMajor()),
                String.valueOf(o.getLevel()),
                slotsStr
            );
        }

        System.out.println();
        
        // let student choose
        System.out.print("Enter opportunity ID of the internship you wish to apply for: ");
        String chosenId = "";

        while (true) {
            chosenId = sc.nextLine().trim();
            boolean found = false;

            for (InternshipOpportunity o : available) {
                if (o.getId().equalsIgnoreCase(chosenId)) {
                    found = true;
                    break;
                }
            }

            if (found) break;  // valid id entered
            System.out.println("The entered opportunity ID cannot be found.");
            System.out.print("Enter opportunity ID of the internship you wish to apply for: ");
        }

        InternshipOpportunity selected = null;
        for (InternshipOpportunity o : available) {
            if (o.getId().equalsIgnoreCase(chosenId)) {
                selected = o;
                break;
            }
        }

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

    private void manageInternshipOffers() {
        ConsoleUI.sectionHeader("Student View > Manage Internship Offers");

        List<Application> offers = applicationService.getSuccessfulOffersForStudent(student);

        if (offers.isEmpty()) {
            System.out.println("✗ You currently have no internship offers.\n");
            System.out.print("Press enter to return... ");
            sc.nextLine();
            ConsoleUI.sectionHeader("Student View");
            return;
        }

        boolean alreadyAccepted = offers.stream().anyMatch(Application::isAccepted);
        if (alreadyAccepted) {
            System.out.println("✓ You have already accepted an internship offer. You cannot accept or reject other internship offers.\n");
            System.out.println();
            System.out.print("Press enter to return... ");
            sc.nextLine();
            return;
        }

        // show all offers first
        System.out.printf("%-4s %-18s %-25s %-18s %-20s%n",
        "S/N", "Application ID", "Internship Title", "Company", "Application Status");
        System.out.println("---------------------------------------------------------------------------------------------------");

        int i = 1;
        for (Application a : offers) {
            System.out.printf("%-4d %-18s %-25s %-18s %-20s%n",
                    i++,   
                    a.getId(),
                    a.getOpportunity().getTitle(),
                    a.getOpportunity().getCompanyName(),
                    a.getStatus());
        }

        // ask if student wants to act
        System.out.print("\nDo you want to accept/reject any internship offers? (y/n): ");
        String response = sc.nextLine().trim().toUpperCase();

        if (!response.equals("Y")) {
            ConsoleUI.sectionHeader("Student View");
        }

        // prompt for specific application ID
        System.out.print("\nEnter Application ID: ");
        String appId = sc.nextLine().trim();

        Application selected = offers.stream()
                .filter(a -> a.getId().equalsIgnoreCase(appId))
                .findFirst()
                .orElse(null);

        if (selected == null) {
            System.out.println("✗ Invalid Application ID. please check and try again.");
            System.out.println();
            System.out.print("Press enter to return... ");
            sc.nextLine();
            ConsoleUI.sectionHeader("Student View");
        }

        // choose accept or reject
        System.out.println("\n(1) Accept Offer");
        System.out.println("(2) Reject Offer");
        System.out.println("(0) Back to Student View");
        System.out.println();
        System.out.print("Enter choice: ");
        String choice = sc.nextLine().trim();

        switch (choice) {
            case "1" -> applicationService.acceptOffer(student, selected);
            case "2" -> applicationService.rejectOffer(student, selected);
            case "0" -> { ConsoleUI.sectionHeader("Student View"); }
            default -> System.out.println("✗ Invalid choice.");
        }

        System.out.print("\nPress enter to return... ");
        sc.nextLine();
        ConsoleUI.sectionHeader("Student View");
    }

    private void withdrawApplication() {
        ConsoleUI.sectionHeader("Student View > Withdraw Internship Applications");
    }
}