package boundary;

import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import control.ApplicationService;
import control.DataLoader;
import control.OpportunityService;
import entity.InternshipOpportunity;
import entity.Student;
import entity.Application;
import entity.User;
import enumerations.ApplicationStatus;
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
                case "2" -> viewAndApplyInternships();
                case "3" -> viewApplications();
                case "4" -> viewPendingInternshipOffers();
                case "5" -> viewAcceptedInternship();
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
        System.out.println("(2) View & Apply Available Internships");
        System.out.println("(3) View My Submitted Applications");
        System.out.println("(4) View Pending Internship Offers");
        System.out.println("(5) View Accepted Internship Placement");
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
            default -> { ConsoleUI.sectionHeader("Student View"); }
        }
    }

    private void viewProfile() {
        ConsoleUI.sectionHeader("Student View > Manage Account > View Profile");
        System.out.println("Student ID    : " + student.getId());
        System.out.println("Name          : " + student.getName());
        System.out.println("Year          : " + student.getYearOfStudy());
        System.out.println("Major         : " + student.getMajor());
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

    private void viewApplications() {
        ConsoleUI.sectionHeader("Student View > View My Submitted Applications");

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
            "%-4s %-15s %-15s %-25s %-20s %-20s %-20s %-20s %-15s%n",
            "S/N", "Application ID", "Opportunity ID", "Internship Title", "Internship Level", "Company", "Preferred Major", "Status", "Applied Date"
        );
        System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------------------------");

        int i = 1;
        for (Application app : myApps) {
            InternshipOpportunity opp = app.getOpportunity();
            System.out.printf(
                "%-4d %-15s %-15s %-25s %-20s %-20s %-20s %-20s %-15s%n",
                i++,
                app.getId(),
                opp.getId(),
                opp.getTitle(),
                String.valueOf(opp.getLevel()),
                opp.getCompanyName(),
                String.valueOf(opp.getPreferredMajor()),
                String.valueOf(app.getStatus()),
                String.valueOf(app.getAppliedAt())
            );
        }
        long activeCount = myApps.stream()
                .filter(a -> a.getStatus() == ApplicationStatus.PENDING 
                        || a.getStatus() == ApplicationStatus.SUCCESSFUL)
                .count();

        System.out.println();
        System.out.println("\n(Total number of active applications: " + activeCount + ")\n");

        System.out.println();
        System.out.print("Press enter to return... ");
        sc.nextLine();
        ConsoleUI.sectionHeader("Student View");
    }

    private void viewPendingInternshipOffers() {
        ConsoleUI.sectionHeader("Student View > View Pending Internship Offers");

        List<Application> offers = applicationService.getSuccessfulOffersForStudent(student);

        if (offers.isEmpty()) {
            System.out.println("✗ You currently have no pending internship offers.\n");
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
        System.out.printf("%-4s %-15s %-15s %-25s %-20s %-20s %-20s %-20s%n",
        "S/N", "Application ID", "Opportunity ID",  "Internship Title", "Internship Level", "Company", "Preferred Major", "Application Status");
        System.out.println("---------------------------------------------------------------------------------------------------");

        int i = 1;
        for (Application a : offers) {
            System.out.printf("%-4s %-15s %-15s %-25s %-20s %-20s %-20s %-20s%n",
                    i++,   
                    a.getId(),
                    a.getOpportunity().getId(),
                    a.getOpportunity().getTitle(),
                    a.getOpportunity().getLevel(),
                    a.getOpportunity().getCompanyName(),
                    a.getOpportunity().getPreferredMajor(),
                    a.getStatus());
        }

        // ask if student wants to act
        System.out.print("\nDo you want to accept/reject any internship opportunities? (y/n): ");
        String response = sc.nextLine().trim().toUpperCase();

        if (!response.equals("Y")) {
            ConsoleUI.sectionHeader("Student View");
            return;
        }

        // prompt for specific application ID
        System.out.print("\nEnter Application ID: ");
        String appId = sc.nextLine().trim();

        Application selected = offers.stream()
                .filter(a -> a.getId().equalsIgnoreCase(appId))
                .findFirst()
                .orElse(null);

        if (selected == null) {
            System.out.println("✗ Invalid Application ID. Please check and try again.");
            System.out.println();
            System.out.print("Press enter to return... ");
            sc.nextLine();
            ConsoleUI.sectionHeader("Student View");
        }

       // print application details
        System.out.println("\n────────────────────────────────────────────────────────────");
        System.out.println("                     APPLICATION DETAILS                   ");
        System.out.println("────────────────────────────────────────────────────────────");
        System.out.printf("%-18s: %s%n", "Application ID", selected.getId());
        System.out.printf("%-18s: %s%n", "Opportunity ID", selected.getOpportunity().getId());
        System.out.printf("%-18s: %s%n", "Internship Title", selected.getOpportunity().getTitle());
        System.out.printf("%-18s: %s%n", "Internship Level", selected.getOpportunity().getLevel());
        System.out.printf("%-18s: %s%n", "Company", selected.getOpportunity().getCompanyName());
        System.out.printf("%-18s: %s%n", "Preferred Major", selected.getOpportunity().getPreferredMajor());
        System.out.printf("%-18s: %s%n", "Status", selected.getStatus());
        System.out.println("────────────────────────────────────────────────────────────");

        // choose accept or reject
        // System.out.println("\n(1) Accept Offer");
        // System.out.println("(2) Reject Offer");
        // System.out.println("(0) Cancel");
        // System.out.println();
        // System.out.print("Enter choice: ");
        // String choice = sc.nextLine().trim();

        // switch (choice) {
        //     case "1" -> applicationService.acceptOffer(student, selected);
        //     case "2" -> applicationService.rejectOffer(student, selected);
        //     case "0" -> { ConsoleUI.sectionHeader("Student View"); }
        //     default -> System.out.println("✗ Invalid choice.");
        // }

        System.out.print("\nAccept this internship opportunity? (y/n): ");
        String decision = sc.nextLine().trim().toLowerCase();

        boolean approve = decision.equals("y") || decision.equals("yes");

        if (approve) {
            applicationService.acceptOffer(student, selected);
        } else {
            applicationService.rejectOffer(student, selected);
        } 

        System.out.print("\nPress enter to return... ");
        sc.nextLine();
        ConsoleUI.sectionHeader("Student View");
    }

    private void withdrawApplication() {
        ConsoleUI.sectionHeader("Student View > Withdraw Internship Applications");

        List<Application> myApps = applicationService.getApplicationsForStudent(student);
        // only allow withdrawal for active applications (pending/successful)
        List<Application> eligible = myApps.stream()
                .filter(a -> a.getStatus() == ApplicationStatus.PENDING
                        || a.getStatus() == ApplicationStatus.SUCCESSFUL)
                .filter(a -> !applicationService.hasPendingWithdrawal(a))
                .collect(Collectors.toList());

        if (eligible.isEmpty()) {
            System.out.println("✗ You have no applications eligible for withdrawal.\n");
            System.out.print("Press enter to return... "); sc.nextLine();
            ConsoleUI.sectionHeader("Student View");
            return;
        }

        System.out.println();
        System.out.printf("%-4s %-15s %-15s %-15s %-25s %-22s %-12s %-10s%n",
                "S/N","Application ID","Opportunity ID","Opportunity ID","Internship Title","Company","Application Status");
        System.out.println("----------------------------------------------------------------------------------------------------------------");
        int i = 1;
        for (Application a : eligible) {
            InternshipOpportunity o = a.getOpportunity();
            System.out.printf("%-4d %-15s %-15s -15s %-25s %-22s %-12s %-10s%n",
                    i++,
                    a.getId(),
                    o.getId(),
                    o.getTitle(),
                    o.getCompanyName(),
                    String.valueOf(a.getStatus()));
        }
        System.out.println();

        System.out.print("Enter Application ID to request withdrawal (blank to cancel): ");
        String appId = sc.nextLine().trim();
        if (appId.isEmpty()) {
            System.out.println("Withdrawal request cancelled.\n");
            ConsoleUI.sectionHeader("Student View");
            return;
        }

        Application selected = eligible.stream()
                .filter(a -> a.getId().equalsIgnoreCase(appId))
                .findFirst().orElse(null);
        if (selected == null) {
            System.out.println("✗ Invalid Application ID.\n");
            System.out.print("Press enter to return... "); sc.nextLine();
            ConsoleUI.sectionHeader("Student View");
            return;
        }

        System.out.printf("Submit withdrawal request for '%s' at %s? (y/n): ",
                selected.getOpportunity().getTitle(),
                selected.getOpportunity().getCompanyName());
        String confirm = sc.nextLine().trim().toLowerCase();
        if (!confirm.equals("y") && !confirm.equals("yes")) {
            System.out.println("Withdrawal request cancelled.\n");
            ConsoleUI.sectionHeader("Student View");
            return;
        }

        boolean ok = applicationService.submitWithdrawalRequest(student, selected);

        System.out.print("Press enter to return... "); sc.nextLine();
        ConsoleUI.sectionHeader("Student View");
    }

    private void viewAcceptedInternship() {
        ConsoleUI.sectionHeader("Student View > View Accepted Internship Placement");

        List<Application> myApps = applicationService.getApplicationsForStudent(student);

        // accepted = company marked successful and student accepted it
        List<Application> accepted = myApps.stream()
                .filter(a -> a.getStatus() == ApplicationStatus.SUCCESSFUL && a.isAccepted())
                .collect(Collectors.toList());

        if (accepted.isEmpty()) {
            System.out.println("✗ You have not accepted any internship.\n");
            System.out.print("Press enter to return... ");
            sc.nextLine();
            ConsoleUI.sectionHeader("Student View");
            return;
        }

        System.out.println("Below are the details of your accepted internship placement: ");
        System.out.println();

        for (Application a : accepted) {
            InternshipOpportunity o = a.getOpportunity();

            System.out.println("\n────────────────────────────────────────────────────────────");
            System.out.println("               INTERNSHIP PLACEMENT DETAILS                   ");
            System.out.println("────────────────────────────────────────────────────────────");
            System.out.printf("%-18s: %s%n", "Application ID", a.getId());
            System.out.printf("%-18s: %s%n", "Opportunity ID", o.getId());
            System.out.printf("%-18s: %s%n", "Internship Title", o.getTitle());
            System.out.printf("%-18s: %s%n", "Internship Level", o.getLevel());
            System.out.printf("%-18s: %s%n", "Company", o.getCompanyName());
            System.out.printf("%-18s: %s%n", "Preferred Major", o.getPreferredMajor());
            System.out.printf("%-18s: %s%n", "Status", a.getStatus());
            System.out.printf("%-18s: %s%n", "Applied At", a.getAppliedAt());
            System.out.printf("%-18s: %s%n", "Decision At", a.getDecisionAt());
            System.out.println("────────────────────────────────────────────────────────────");
        }

        System.out.print("\nPress enter to return... ");
        sc.nextLine();
        ConsoleUI.sectionHeader("Student View");
    }

    private void viewAndApplyInternships() {
        ConsoleUI.sectionHeader("Student View > View & Apply internships");

        // fetch opportunities open to this student
        List<InternshipOpportunity> available = opportunityService.getAllOpportunities().stream()
                .filter(o -> o.isOpenFor(student))
                .collect(Collectors.toList());

        // get student's applications and build a set of opportunity ids already applied to
        // rule: hide anything the student has applied to (pending/successful/unsuccessful);
        // allow re-apply only if they previously withdrew.
        List<Application> myApps = applicationService.getApplicationsForStudent(student);
        // remove only actively applied opportunities (pending or successful)
        Set<String> appliedOppIds = applicationService.getApplicationsForStudent(student).stream()
                .filter(a -> a.getStatus() == ApplicationStatus.PENDING
                        || a.getStatus() == ApplicationStatus.SUCCESSFUL)
                .map(a -> a.getOpportunity().getId())
                .collect(Collectors.toSet());

        // show all others, including unsuccessful or withdrawn ones
        available = available.stream()
                .filter(o -> !appliedOppIds.contains(o.getId()))
                .collect(Collectors.toList());

        if (available.isEmpty()) {
            System.out.println("✗ No internship opportunities currently available for your profile.\n");
            System.out.print("Press enter key to continue... "); sc.nextLine();
            ConsoleUI.sectionHeader("Student View");
            return;
        }

        // print list
        System.out.println();
        System.out.printf("%-4s %-15s %-25s %-20s %-20s %-20s %-20s%n",
                "S/N", "Opportunity ID", "Internship Title", "Internship Level", "Company", "Preferred Major", "Available Slots");
        System.out.println("------------------------------------------------------------------------------------------------------------------------------");

        int i = 1;
        for (InternshipOpportunity o : available) {
            String slotsStr = String.format("%d/%d", o.getConfirmedSlots(), o.getSlots());
            System.out.printf("%-4d %-15s %-25s %-20s %-20s %-20s %-20s%n",
                    i++, o.getId(), o.getTitle(), String.valueOf(o.getLevel()), o.getCompanyName(),
                    String.valueOf(o.getPreferredMajor()), slotsStr);
        }
        System.out.println("\n(Total: " + available.size() + " internship opportunities)\n");

        // check if student has already accepted an internship (cannot apply anymore)
        boolean hasAccepted = myApps.stream()
                .anyMatch(a -> a.getStatus() == ApplicationStatus.SUCCESSFUL && a.isAccepted());
        if (hasAccepted) {
            System.out.println("Note: You have already accepted an internship placement.");
            System.out.println("You may browse available internship opportunities, but cannot apply for new ones.\n");
            System.out.print("Press enter key to continue... "); sc.nextLine();
            ConsoleUI.sectionHeader("Student View");
            return; // viewing only; no apply prompt
        }

        // enforce max 3 active applications (pending or successful)
        long activeCount = myApps.stream()
                .filter(a -> a.getStatus() == ApplicationStatus.PENDING
                        || a.getStatus() == ApplicationStatus.SUCCESSFUL)
                .count();
        if (activeCount >= 3) {
            System.out.println("✗ You have reached the maximum of 3 active applications.\n");
            System.out.print("Press enter key to continue... "); 
            sc.nextLine();
            ConsoleUI.sectionHeader("Student View");
            return;
        }

        // ask if the student wants to apply now
        System.out.print("Do you want to apply for an internship now? (y/n): ");
        String ans = sc.nextLine().trim().toLowerCase();
        if (!ans.equals("y") && !ans.equals("yes")) {
            System.out.println();
            ConsoleUI.sectionHeader("Student View");
            return;
        }

        // prompt for id
        System.out.print("Enter Opportunity ID of the internship you wish to apply for: ");
        String chosenId = "";
        boolean found;
        while (true) {
            chosenId = sc.nextLine().trim();
            found = false;
            for (InternshipOpportunity o : available) {
                if (o.getId().equalsIgnoreCase(chosenId)) { found = true; break; }
            }
            if (found) break;
            System.out.println("The entered Opportunity ID cannot be found.");
            System.out.print("Enter Opportunity ID of the internship you wish to apply for: ");
        }

        // get selected object
        InternshipOpportunity selected = null;
        for (InternshipOpportunity o : available) {
            if (o.getId().equalsIgnoreCase(chosenId)) { selected = o; break; }
        }

        // show compact summary before confirming
        System.out.println("\n──────────────────────────────────────────────");
        System.out.println("              APPLICATION DETAILS              ");
        System.out.println("──────────────────────────────────────────────");
        System.out.printf("%-20s: %s%n", "Student ID", student.getId());
        System.out.printf("%-20s: %s%n", "Opportunity ID", selected.getId());
        System.out.printf("%-20s: %s%n", "Internship Title", selected.getTitle());
        System.out.printf("%-20s: %s%n", "Internship Level", selected.getLevel());
        System.out.printf("%-20s: %s%n", "Company", selected.getCompanyName());
        System.out.printf("%-20s: %s%n", "Preferred Major", selected.getPreferredMajor());
        System.out.println("──────────────────────────────────────────────\n");

        System.out.print("Confirm apply to this internship? (y/n): ");
        String confirm = sc.nextLine().trim().toLowerCase();

        if (confirm.equals("y") || confirm.equals("yes")) {
            Application result = applicationService.applyForOpportunity(student, selected);
            if (result != null) {
                System.out.println("\n✓ Application submitted successfully – " + result.getId());
                ConsoleUI.sectionHeader("Student View");
            } else {
                long active = applicationService.getActiveCountForStudent(student.getId());
                boolean duplicate = applicationService.hasActiveApplication(student, selected);
                if (duplicate) {
                    System.out.println("✗ You already have an active application for this opportunity.\n");
                    ConsoleUI.sectionHeader("Student View");
                } else if (active >= 3) {
                    System.out.println("✗ You have reached the maximum of 3 active applications.\n");
                    ConsoleUI.sectionHeader("Student View");
                } else {
                    System.out.println("✗ Unable to submit application. Please try again later.\n");
                    ConsoleUI.sectionHeader("Student View");
                }
            }
        } else {
            System.out.println("Application cancelled.\n");
            ConsoleUI.sectionHeader("Student View");
        }
            }
}