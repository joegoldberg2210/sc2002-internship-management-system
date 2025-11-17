package boundary;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.stream.Collectors;

import control.ApplicationService;
import control.OpportunityService;
import control.DataLoader;
import entity.CompanyRepresentative;
import entity.InternshipOpportunity;
import entity.User;
import entity.Application;
import entity.FilterCriteria;
import enumerations.InternshipLevel;
import enumerations.Major;
import enumerations.OpportunityStatus;
import ui.ConsoleUI; 

public class CompanyRepView {
    private final Scanner sc;
    private final CompanyRepresentative rep;
    private final OpportunityService opportunityService;
    private final ApplicationService applicationService;
    private final List<User> users;
    private final DataLoader loader;


    private final FilterCriteria myOppFilter = new FilterCriteria();
    private String myOppSortKey = "title";   
    private boolean myOppSortDescending = false;

    public CompanyRepView(Scanner sc, CompanyRepresentative rep, List<User> users, DataLoader loader, OpportunityService opportunityService, ApplicationService applicationService) {
        this.sc = sc;
        this.rep = rep;
        this.users = users;
        this.loader = loader;
        this.opportunityService = opportunityService;
        this.applicationService = applicationService;
    }

    public void run() {
        while (rep.isFirstLogin()) {
            forceFirstTimePasswordChange();
        }

        ConsoleUI.sectionHeader("Company Representative View");

        boolean running = true;
        while (running) {
            showMenu();
            System.out.print("Enter choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> manageAccount();
                case "2" -> manageOpportunities();
                case "3" -> reviewPendingApplications();
                case "4" -> viewAllApplications();
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
        System.out.println("(2) Manage Internship Opportunities");
        System.out.println("(3) Review Pending Internship Applications");
        System.out.println("(4) View All Internship Applications");
        System.out.println();
        System.out.println("→ Type 'logout' here to logout");
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
            case "0" -> { ConsoleUI.sectionHeader("Company Representative View"); }
            default -> { ConsoleUI.sectionHeader("Company Representative View"); }
        }
    }

    private void viewProfile() {
        ConsoleUI.sectionHeader("Company Representative View > Manage Account > View Profile");
        System.out.println("Company Representative ID    : " + rep.getId());
        System.out.println("Name                         : " + rep.getName());
        System.out.println("Company                      : " + rep.getCompanyName());
        System.out.println("Department                   : " + rep.getDepartment());
        System.out.println("Position                     : " + rep.getPosition());
        System.out.println();
        System.out.print("Press enter key to continue... ");
        sc.nextLine();

        ConsoleUI.sectionHeader("Company Representative View");
    }

    private void changePassword() {
        ConsoleUI.sectionHeader("Company Representative View > Manage Account > Change Password");

        String current;
        while (true) {
            System.out.print("Enter current password: ");
            current = sc.nextLine().trim();
            if (current.isEmpty()) {
                System.out.println("✗ Current password cannot be empty.\n");
                continue;
            }
            break;
        }

        String newPwd;
        while (true) {
            System.out.print("Enter new password: ");
            newPwd = sc.nextLine().trim();
            if (newPwd.isEmpty()) {
                System.out.println("✗ New password cannot be empty.\n");
                continue;
            }
            if (newPwd.equals(current)) {
                System.out.println("✗ New password cannot be the same as your current password.\n");
                continue;
            }
            break;
        }

        String confirm;
        while (true) {
            System.out.print("Confirm new password: ");
            confirm = sc.nextLine().trim();
            if (confirm.isEmpty()) {
                System.out.println("✗ Confirm new password cannot be empty.\n");
                continue;
            }
            break;
        }

        boolean successful = newPwd.equals(confirm) && rep.changePassword(current, newPwd);

        if (successful) {
            rep.setFirstLogin(false);
            System.out.println("\n✓ Password changed successfully!");
            loader.saveUsers(users);
            ConsoleUI.sectionHeader("Company Representative View");
        } else {
            System.out.println("\n✗ Unable to change password. Please try again.");
            ConsoleUI.sectionHeader("Company Representative View");
        }
    }

    private void manageOpportunities() {
        ConsoleUI.sectionHeader("Company Representative View > Manage Internship Opportunities");
        System.out.println("(1) Create New Internship Opportunity");
        System.out.println("(2) View My Internship Opportunities");
        System.out.println("(3) Edit Existing Internship Opportunity");
        System.out.println("(4) Delete Opportunity");
        System.out.println("(5) Toggle Visibility");
        System.out.println("(0) Back to Company Representative View");
        System.out.println();
        System.out.print("Enter choice: ");
        String c = sc.nextLine().trim();

        switch (c) {
            case "1" -> createOpportunity();
            case "2" -> viewMyOpportunities();
            case "3" -> editOpportunity();
            case "4" -> deleteOpportunity();
            case "5" -> toggleVisibility();
            case "0" -> { ConsoleUI.sectionHeader("Company Representative View"); }
            default -> System.out.println("✗ Invalid choice.\n");
        }
    }

    private void reviewPendingApplications() {
        ConsoleUI.sectionHeader("Company Representative View > Review Internship Applications");
        System.out.println();

        List<Application> myApps = applicationService.getApplicationsByRepresentative(rep)
            .stream()
            .filter(Application::isActive)
            .collect(Collectors.toList());

        if (myApps.isEmpty()) {
            System.out.println("✗ No applications to review.\n");
            System.out.print("Press enter to return... ");
            sc.nextLine();
            ConsoleUI.sectionHeader("Company Representative View");
            return;
        }


        System.out.printf(
            "%-4s %-15s %-15s %-15s %-20s %-20s %-20s %-20s %-20s %-12s%n",
            "S/N", "Application ID", "Student ID", "Opportunity ID", "Internship Title", "Internship Level", "Company", "Preferred Major", "Number of Slots", "Applied At"
        );
        System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

        int i = 1;
        for (Application a : myApps) {
            String slotsStr = String.format("%d/%d", a.getOpportunity().getConfirmedSlots(), a.getOpportunity().getSlots());
            System.out.printf(
                "%-4s %-15s %-15s %-15s %-20s %-20s %-20s %-20s %-20s %-12s%n",
                i++,
                a.getId(),
                a.getStudent().getId(),
                a.getOpportunity().getId(),
                a.getOpportunity().getTitle(),
                a.getOpportunity().getLevel(),
                a.getOpportunity().getCompanyName(),
                a.getStatus(),
                slotsStr,
                a.getAppliedAt()
            );
        }

        System.out.println("\n(Total: " + myApps.size() + " internship applications)\n");

        Application selected = null;

        while (true) {
            System.out.print("Enter Application ID to review (blank to cancel): ");
            String id = sc.nextLine().trim();

            if (id.isEmpty()) {
                ConsoleUI.sectionHeader("Company Representative View");
                return;
            }

            selected = myApps.stream()
                    .filter(a -> a.getId().equalsIgnoreCase(id))
                    .findFirst()
                    .orElse(null);

            if (selected != null) break;

            System.out.println("✗ Invalid application ID. Please try again.\n");
        }

        InternshipOpportunity opp = selected.getOpportunity();

        System.out.println("\n────────────────────────────────────────────────────────────");
        System.out.println("                    Application Details                      ");
        System.out.println("────────────────────────────────────────────────────────────");
        System.out.printf("%-18s: %s%n", "Application ID", selected.getId());
        System.out.printf("%-18s: %s%n", "Student ID", selected.getStudent().getId());
        System.out.printf("%-18s: %s%n", "Opportunity ID", opp.getId());
        System.out.printf("%-18s: %s%n", "Internship Title", opp.getTitle());
        System.out.printf("%-18s: %s%n", "Internship Level", opp.getLevel());
        System.out.printf("%-18s: %s%n", "Company", opp.getCompanyName());
        System.out.printf("%-18s: %s%n", "Preferred Major", opp.getPreferredMajor());
        System.out.printf("%-18s: %s%n", "Status", selected.getStatus());
        System.out.printf("%-18s: %s%n", "Applied At", selected.getAppliedAt());
        System.out.println("────────────────────────────────────────────────────────────\n");

        String decision;
        boolean approve;

        while (true) {
            System.out.print("\nApprove this internship application? (y/n): ");
            decision = sc.nextLine().trim().toLowerCase();

            if (decision.equals("y") || decision.equals("yes")) {
                approve = true;
                break;
            } 
            if (decision.equals("n") || decision.equals("no")) {
                approve = false;
                break;
            }

            System.out.println("✗ Invalid input. Please try again.\n");
        }

        applicationService.decideApplication(rep, selected, approve);

        ConsoleUI.sectionHeader("Company Representative View");
    }

    private void createOpportunity() {
        ConsoleUI.sectionHeader("Company Representative View > Create New Internship Opportunity");
        
        long activeCount = opportunityService.getByCompany(rep.getCompanyName()).size();
        if (activeCount >= 5) {
            System.out.println("✗ You are unable to create new internship opportunity as you already have 5 active opportunities.\n");
            System.out.print("Press enter key to continue... ");
            sc.nextLine(); 
            ConsoleUI.sectionHeader("Company Representative View");
            return;
        }

        String title = "";
        while (true) {
            System.out.print("Title (leave blank to cancel): ");
            title = sc.nextLine().trim();

            if (title.isEmpty()) {
                System.out.println("Creation cancelled.\n");
                ConsoleUI.sectionHeader("Company Representative View");
                return;
            }

            if (title.length() < 3) {
                System.out.println("✗ Title must be at least 3 characters.\n");
                continue;
            }

            break; 
        }

        System.out.print("Description: ");
        String description = sc.nextLine().trim();

        Major preferredMajor = selectMajor();

        InternshipLevel level = selectLevel();

        int slots = 0;

        while (true) {
            System.out.print("Enter number of slots (1-10): ");
            String input = sc.nextLine().trim();

            try {
                slots = Integer.parseInt(input);
                if (slots >= 1 && slots <= 10) {
                    break;
                } else {
                    System.out.println("✗ Invalid number of slots");
                }
            } catch (NumberFormatException e) {
                System.out.println("✗ Please enter a valid integer.");
            }
        }

        InternshipOpportunity opp = new InternshipOpportunity(
            UUID.randomUUID().toString().substring(0, 6),
            title,
            description,
            level,
            preferredMajor,                         
            LocalDate.now(),
            LocalDate.now().plusMonths(3),
            rep.getCompanyName(),
            slots,
            rep
        );

        opportunityService.createOpportunity(rep, opp);

        ConsoleUI.sectionHeader("Company Representative View");
    }

    private void editOpportunity() {
        boolean continueEditing = true;

        while (continueEditing) {
            ConsoleUI.sectionHeader("Company Representative View > Edit Pending Internship Opportunity");

            List<InternshipOpportunity> myOpps = opportunityService.getAllOpportunities().stream()
                    .filter(o -> rep.equals(o.getRepInCharge()))
                    .filter(o -> o.getStatus() == OpportunityStatus.PENDING)
                    .collect(Collectors.toList());

            if (myOpps.isEmpty()) {
                System.out.println("✗ No pending internship opportunities available for editing.\n");
                ConsoleUI.sectionHeader("Company Representative View");
                return;
            }

            System.out.println();
            System.out.printf(
                "%-4s %-15s %-25s %-20s %-20s %-20s %-15s %-15s %-15s%n",
                "S/N", "Opportunity ID", "Internship Title", "Level", "Company",
                "Preferred Major", "Total Slots", "Open Date", "Close Date"
            );
            System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------");

            int i = 1;
            for (InternshipOpportunity o : myOpps) {
                System.out.printf(
                    "%-4s %-15s %-25s %-20s %-20s %-20s %-15s %-15s %-15s%n",
                    i++,
                    o.getId(),
                    o.getTitle(),
                    String.valueOf(o.getLevel()),
                    o.getCompanyName(),
                    String.valueOf(o.getPreferredMajor()),
                    o.getSlots(),
                    o.getOpenDate(),
                    o.getCloseDate()
                );
            }
            System.out.println();

            InternshipOpportunity existing = null;
            while (true) {
                System.out.print("Enter Opportunity ID to edit (leave blank to cancel): ");
                String id = sc.nextLine().trim();
                if (id.isEmpty()) {
                    ConsoleUI.sectionHeader("Company Representative View");
                    return;
                }

                existing = opportunityService.findById(id);
                if (existing == null) {
                    System.out.println("✗ Opportunity ID not found.\n");
                    continue;
                }
                break; 
            }

            boolean editing = true;
            while (editing) {
                System.out.println();
                System.out.println("────────────────────────────────────────────────────────────");
                System.out.println("              Internship Opportunity Details                ");
                System.out.println("────────────────────────────────────────────────────────────");
                System.out.printf("%-18s: %s%n", "(1) Internship Title", existing.getTitle());
                System.out.printf("%-18s: %s%n", "(2) Description", existing.getDescription());
                System.out.printf("%-18s: %s%n", "(3) Internship Level", existing.getLevel());
                System.out.printf("%-18s: %s%n", "(4) Preferred Major", existing.getPreferredMajor());
                System.out.printf("%-18s: %d%n", "(5) Total Slots", existing.getSlots());
                System.out.printf("%-18s: %s%n", "(6) Open Date", existing.getOpenDate());
                System.out.printf("%-18s: %s%n", "(7) Close Date", existing.getCloseDate());
                System.out.println("────────────────────────────────────────────────────────────");
                System.out.println();
                System.out.print("Choose a field number to edit, or 0 to save & go back: ");

                String choice = sc.nextLine().trim();

                switch (choice) {
                    case "1" -> {
                        System.out.print("Enter new Title: ");
                        existing.setTitle(sc.nextLine().trim());
                    }
                    case "2" -> {
                        System.out.print("Enter new Description: ");
                        existing.setDescription(sc.nextLine().trim());
                    }
                    case "3" -> {
                        InternshipLevel l = selectLevel();
                        existing.setLevel(l);
                    }
                    case "4" -> {
                        Major m = selectMajor();
                        existing.setPreferredMajor(m);
                    }
                    case "5" -> {
                        System.out.print("Enter new number of slots: ");
                        try {
                            existing.setSlots(Integer.parseInt(sc.nextLine().trim()));
                        } catch (NumberFormatException e) {
                            System.out.println("✗ Invalid number. Number of slots not changed.");
                        }
                    }
                    case "6" -> {
                        System.out.print("Enter new Open Date (YYYY-MM-DD): ");
                        try {
                            existing.setOpenDate(LocalDate.parse(sc.nextLine().trim()));
                        } catch (Exception e) {
                            System.out.println("✗ Invalid date format. Open date not changed.");
                        }
                    }
                    case "7" -> {
                        System.out.print("Enter new Close Date (YYYY-MM-DD): ");
                        try {
                            existing.setCloseDate(LocalDate.parse(sc.nextLine().trim()));
                        } catch (Exception e) {
                            System.out.println("✗ Invalid date format. Close date not changed.");
                        }
                    }
                    case "0" -> {
                        editing = false;
                        opportunityService.editOpportunity(rep, existing);
                        System.out.println("✓ All edits saved.");
                    }
                    default -> System.out.println("✗ Invalid choice, try again.");
                }
            }

            System.out.println();
            while (true) {
                System.out.print("Do you want to edit another internship opportunity? (y/n): ");
                String again = sc.nextLine().trim().toLowerCase();

                if (again.equals("y") || again.equals("yes")) {
                    continueEditing = true;
                    break;
                }

                if (again.equals("n") || again.equals("no")) {
                    continueEditing = false;
                    ConsoleUI.sectionHeader("Company Representative View");
                    return;
                }

                System.out.println("✗ Invalid choice. Please try again.\n");
            }
        }
    }

    private void deleteOpportunity() {
        while (true) {
            ConsoleUI.sectionHeader("Company Representative View > Delete Pending Internship Opportunity");

            List<InternshipOpportunity> myOpps = opportunityService.getAllOpportunities().stream()
                    .filter(o -> rep.equals(o.getRepInCharge()))
                    .filter(o -> o.getStatus() == OpportunityStatus.PENDING)
                    .collect(Collectors.toList());

            if (myOpps.isEmpty()) {
                System.out.println("✗ No pending internship opportunities available for deletion.\n");
                ConsoleUI.sectionHeader("Company Representative View");
                return;
            }

            System.out.println();
            System.out.printf(
                "%-4s %-15s %-25s %-20s %-20s %-20s %-15s %-15s %-15s%n",
                "S/N", "Opportunity ID", "Internship Title", "Level", "Company",
                "Preferred Major", "Total Slots", "Open Date", "Close Date"
            );
            System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------");

            int i = 1;
            for (InternshipOpportunity o : myOpps) {
                System.out.printf(
                    "%-4d %-15s %-25s %-20s %-20s %-20s %-15d %-15s %-15s%n",
                    i++,
                    o.getId(),
                    o.getTitle(),
                    o.getLevel(),
                    o.getCompanyName(),
                    o.getPreferredMajor(),
                    o.getSlots(),
                    o.getOpenDate(),
                    o.getCloseDate()
                );
            }

            InternshipOpportunity existing = null;

            while (true) {
                System.out.print("\nEnter Opportunity ID to delete (leave blank to cancel): ");
                String id = sc.nextLine().trim();

                if (id.isEmpty()) {
                    System.out.println("Deletion cancelled.\n");
                    ConsoleUI.sectionHeader("Company Representative View");
                    return;
                }

                existing = opportunityService.findById(id);

                if (existing == null || !rep.equals(existing.getRepInCharge())) {
                    System.out.println("✗ Invalid Opportunity ID. Please try again.\n");
                    continue;
                }

                break;
            }

            System.out.printf("Are you sure you want to delete '%s' (%s)? (y/n): ",
                    existing.getTitle(), existing.getId());
            String confirm = sc.nextLine().trim().toLowerCase();

            if (confirm.equals("y") || confirm.equals("yes")) {
                opportunityService.deleteOpportunity(rep, existing.getId());
            } else {
                System.out.println("Deletion cancelled.\n");
                ConsoleUI.sectionHeader("Company Representative View");
                return;
            }

            while (true) {
                System.out.print("\nDo you want to delete another internship opportunity? (y/n): ");
                String again = sc.nextLine().trim().toLowerCase();

                if (again.equals("y") || again.equals("yes")) {
                    break; 
                }

                if (again.equals("n") || again.equals("no")) {
                    ConsoleUI.sectionHeader("Company Representative View");
                    return;
                }

                System.out.println("✗ Invalid choice. Please try again.\n");
            }
        }
    }

    private void toggleVisibility() {
        ConsoleUI.sectionHeader("Company Representative View > Toggle Opportunity Visibility");

        List<InternshipOpportunity> myOpps = opportunityService.getAllOpportunities().stream()
                .filter(o -> rep.equals(o.getRepInCharge()))
                .collect(Collectors.toList());

        if (myOpps.isEmpty()) {
            System.out.println("✗ You currently have no internship opportunities.\n");
            ConsoleUI.sectionHeader("company representative view");
            return;
        }

        System.out.println();
        System.out.printf(
            "%-4s %-15s %-25s %-20s %-20s %-20s %-15s %-15s %-15s %-10s %-15s%n",
            "S/N", "Opportunity ID", "Internship Title", "Level", "Company",
            "Preferred Major", "Number of Slots", "Open Date", "Close Date", "Status", "Visibility"
        );
        System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

        int i = 1;
        for (InternshipOpportunity o : myOpps) {
            String slotsStr = String.format("%d/%d", o.getConfirmedSlots(), o.getSlots());
            System.out.printf(
                "%-4s %-15s %-25s %-20s %-20s %-20s %-15s %-15s %-15s %-10s %-15s%n",
                i++,
                o.getId(),
                o.getTitle(),
                String.valueOf(o.getLevel()),
                o.getCompanyName(),
                String.valueOf(o.getPreferredMajor()),
                slotsStr,
                o.getOpenDate(),
                o.getCloseDate(),
                o.getStatus(),
                o.isVisible() ? "ON" : "OFF"
            );
        }

        InternshipOpportunity opp = null;

        while (true) {
            System.out.print("\nEnter Opportunity ID to toggle visibility (leave blank to cancel): ");
            String id = sc.nextLine().trim();

            if (id.isEmpty()) {
                System.out.println("Action cancelled.\n");
                ConsoleUI.sectionHeader("Company Representative View");
                return;
            }

            opp = opportunityService.findById(id);

            if (opp == null) {
                System.out.println("✗ Invalid Opportunity ID. Please try again.\n");
                continue;
            }

            if (opp.getStatus() == OpportunityStatus.REJECTED) {
                System.out.println("✗ You cannot change visibility for a REJECTED opportunity. Please try again.");
                continue;
            }

            if (opp.getStatus() == OpportunityStatus.PENDING) {
                System.out.println("✗ Visibility cannot be changed until this opportunity is APPROVED. Please try again.");
                continue;
            }

            if (opp.getStatus() == OpportunityStatus.FILLED) {
                System.out.println("✗ You cannot change visibility for a FILLED opportunity. Please try again.");
                continue;
            }

            break;
        }

        boolean newState = !opp.isVisible();
        opportunityService.toggleVisibility(opp, newState);
        System.out.println("✓ Visibility for '" + opp.getTitle() + "' changed to " + (newState ? "ON" : "OFF") + ".\n");

        System.out.print("Press enter to return... ");
        sc.nextLine();
        ConsoleUI.sectionHeader("Company Representative View");
    }

    private void viewMyOpportunities() {
        ConsoleUI.sectionHeader("Company Representative View > View My Internship Opportunities");

        while (true) {
            List<InternshipOpportunity> myOpps =
                    opportunityService.getByCompany(rep.getCompanyName());

            myOpps = myOpps.stream()
                    .filter(o -> myOppFilter.getLevel() == null ||
                            o.getLevel() == myOppFilter.getLevel())
                    .filter(o -> myOppFilter.getStatus() == null ||
                            o.getStatus() == myOppFilter.getStatus())
                    .filter(o -> myOppFilter.getPreferredMajor() == null ||
                            o.getPreferredMajor() == myOppFilter.getPreferredMajor())
                    .collect(Collectors.toList());

            Comparator<InternshipOpportunity> cmp;
            if ("slots".equalsIgnoreCase(myOppSortKey)) {
                cmp = Comparator.comparingInt(InternshipOpportunity::getSlots);
            } else if ("openDate".equalsIgnoreCase(myOppSortKey)) {
                cmp = Comparator.comparing(InternshipOpportunity::getOpenDate);
            } else if ("closeDate".equalsIgnoreCase(myOppSortKey)) {
                cmp = Comparator.comparing(InternshipOpportunity::getCloseDate);
            } else {
                cmp = Comparator.comparing(InternshipOpportunity::getTitle,
                        String.CASE_INSENSITIVE_ORDER);
                myOppSortKey = "title";
            }
            if (myOppSortDescending) cmp = cmp.reversed();
            myOpps.sort(cmp);

            printMyOppFilterAndSort(myOppFilter, myOppSortKey, myOppSortDescending);
            printMyOpportunitiesTable(myOpps);

            System.out.println("(1) Edit Filter");
            System.out.println("(2) Edit Sort");
            System.out.println("(0) Reset Filter & Sort");  
            System.out.print("Enter choice (blank to cancel): ");
            String choice = sc.nextLine().trim();

            if (choice.isEmpty()) {
                ConsoleUI.sectionHeader("Company Representative View");
                return;
            }

            else if ("1".equals(choice)) {
                while (true) {
                    System.out.println();
                    System.out.println("Filter opportunities by:");
                    System.out.println("(1) Internship Level");
                    System.out.println("(2) Opportunity Status");
                    System.out.println("(3) Preferred Major");
                    System.out.print("Enter choice (blank to cancel): ");
                    String f = sc.nextLine().trim();

                    if (f.isEmpty()) break;

                    if ("1".equals(f)) {
                        System.out.println();
                        System.out.println("Select Internship Level:");
                        System.out.println("(1) Basic");
                        System.out.println("(2) Intermediate");
                        System.out.println("(3) Advanced");
                        System.out.print("Enter choice (blank to cancel): ");
                        String lv = sc.nextLine().trim();

                        switch (lv) {
                            case "1" -> { myOppFilter.setLevel(InternshipLevel.BASIC);
                                System.out.println("✓ Level filter set to BASIC.\n"); }
                            case "2" -> { myOppFilter.setLevel(InternshipLevel.INTERMEDIATE);
                                System.out.println("✓ Level filter set to INTERMEDIATE.\n"); }
                            case "3" -> { myOppFilter.setLevel(InternshipLevel.ADVANCED);
                                System.out.println("✓ Level filter set to ADVANCED.\n"); }
                            case "" -> System.out.println("Level unchanged.\n");        
                            default -> System.out.println("✗ Invalid option.\n");
                        }
                        break;
                    }

                    else if ("2".equals(f)) {
                        System.out.println();
                        System.out.println("Select Opportunity Status:");
                        System.out.println("(1) Pending)");
                        System.out.println("(2) Approved)");
                        System.out.println("(3) Filled)");
                        System.out.println("(4) Rejected)");
                        System.out.print("Enter choice (blank to cancel): ");
                        String sf = sc.nextLine().trim();

                        switch (sf) {
                            case "1" -> { myOppFilter.setStatus(OpportunityStatus.PENDING);
                                System.out.println("✓ Status filter set to PENDING.\n"); }
                            case "2" -> { myOppFilter.setStatus(OpportunityStatus.APPROVED);
                                System.out.println("✓ Status filter set to APPROVED.\n"); }
                            case "3" -> { myOppFilter.setStatus(OpportunityStatus.FILLED);
                                System.out.println("✓ Status filter set to FILLED.\n"); }
                            case "4" -> { myOppFilter.setStatus(OpportunityStatus.REJECTED);
                                System.out.println("✓ Status filter set to REJECTED.\n"); }
                            case "" -> System.out.println("Status unchanged.\n");        
                            default -> System.out.println("✗ Invalid option.\n");
                        }
                        break;
                    }

                    else if ("3".equals(f)) {
                        System.out.println();
                        System.out.println("Select Preferred Major:");
                        System.out.println("(1) CSC");
                        System.out.println("(2) DSAI");
                        System.out.println("(3) CEG");
                        System.out.println("(4) IEM");
                        System.out.println("(5) BCG");
                        System.out.println("(6) BCE");
                        System.out.print("Enter choice (blank to cancel): ");
                        String mj = sc.nextLine().trim();

                        switch (mj) {
                            case "1" -> { myOppFilter.setPreferredMajor(Major.CSC);
                                System.out.println("✓ Preferred major set to CSC.\n"); }
                            case "2" -> { myOppFilter.setPreferredMajor(Major.DSAI);
                                System.out.println("✓ Preferred major set to DSAI.\n"); }
                            case "3" -> { myOppFilter.setPreferredMajor(Major.CEG);
                                System.out.println("✓ Preferred major set to CEG.\n"); }
                            case "4" -> { myOppFilter.setPreferredMajor(Major.IEM);
                                System.out.println("✓ Preferred major set to IEM.\n"); }
                            case "5" -> { myOppFilter.setPreferredMajor(Major.BCG);
                                System.out.println("✓ Preferred major set to BCG.\n"); }
                            case "6" -> { myOppFilter.setPreferredMajor(Major.BCE);
                                System.out.println("✓ Preferred major set to BCE.\n"); }
                            case "" -> System.out.println("Preferred major unchanged.\n"); 
                            default -> System.out.println("✗ Invalid option.\n");
                        }
                        break;
                    }
                }
            }
            else if ("2".equals(choice)) {
                System.out.println();
                System.out.println("Sort opportunities by:");
                System.out.println("(1) Internship Title");
                System.out.println("(2) Number of Slots");
                System.out.println("(3) Open Date");
                System.out.println("(4) Close Date");
                System.out.print("Enter choice (blank to cancel): ");
                String s = sc.nextLine().trim();

                if (!s.isEmpty()) {
                    switch (s) {
                        case "1" -> myOppSortKey = "title";
                        case "2" -> myOppSortKey = "slots";
                        case "3" -> myOppSortKey = "openDate";
                        case "4" -> myOppSortKey = "closeDate";
                        default -> System.out.println("✗ Invalid option.");
                    }

                    if ("1".equals(s) || "2".equals(s) || "3".equals(s) || "4".equals(s)) {
                        System.out.println();
                        System.out.println("Sort Order:");
                        System.out.println("(1) Ascending");
                        System.out.println("(2) Descending");
                        System.out.print("Enter choice (blank = ascending): ");
                        String order = sc.nextLine().trim();

                        if (order.isEmpty() || "1".equals(order)) myOppSortDescending = false;
                        else if ("2".equals(order)) myOppSortDescending = true;
                    }
                }
            }

            else if ("0".equals(choice)) {
                myOppFilter.setLevel(null);
                myOppFilter.setStatus(null);
                myOppFilter.setPreferredMajor(null);
                myOppSortKey = "title";
                myOppSortDescending = false;
                System.out.println("\n✓ Filters & sorting reset.\n");
            }

            else {
                System.out.println("✗ Invalid choice.\n");
            }
        }
    }

    /** 
     * @param filter
     * @param sortKey
     * @param sortDescending
     */
    private void printMyOppFilterAndSort(FilterCriteria filter,String sortKey,boolean sortDescending) {
        StringBuilder fb = new StringBuilder("Current Filters: ");
        boolean any = false;

        if (filter.getLevel() != null) {
            fb.append("Internship Level = ")
            .append(filter.getLevel().name().toLowerCase())
            .append("  ");
            any = true;
        }
        if (filter.getStatus() != null) {
            fb.append("Status = ")
            .append(filter.getStatus().name().toLowerCase())
            .append("  ");
            any = true;
        }
        if (filter.getPreferredMajor() != null) {
            fb.append("Preferred Major = ")
            .append(filter.getPreferredMajor())
            .append("  ");
            any = true;
        }

        if (!any) {
            fb = new StringBuilder("Current Filters: None");
        }

        String sortLabel;
        if ("slots".equalsIgnoreCase(sortKey)) {
            sortLabel = "Number of Slots";
        } else if ("openDate".equalsIgnoreCase(sortKey)) {
            sortLabel = "Open Date";
        } else if ("closeDate".equalsIgnoreCase(sortKey)) {
            sortLabel = "Close Date";
        } else {
            sortLabel = "Internship Title";
        }

        String sortInfo = "Current Sorting: "
                + sortLabel
                + " (" + (sortDescending ? "Descending" : "Ascending") + ")";

        System.out.println(fb.toString());
        System.out.println(sortInfo);
        System.out.println();
    }

    /** 
     * @param myOpps
     */
    private void printMyOpportunitiesTable(List<InternshipOpportunity> myOpps) {
        if (myOpps.isEmpty()) {
            System.out.println("✗ No internship opportunities match your current filters.\n");
            return;
        }

        System.out.printf(
            "%-4s %-15s %-25s %-20s %-20s %-20s %-15s %-15s %-15s %-10s %-15s%n",
            "S/N", "Opportunity ID", "Internship Title", "Level", "Company",
            "Preferred Major", "Number of Slots", "Open Date", "Close Date", "Status", "Visibility"
        );
        System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

        int i = 1;
        for (InternshipOpportunity o : myOpps) {
            String slotsStr = String.format("%d/%d", o.getConfirmedSlots(), o.getSlots());
            System.out.printf(
                "%-4d %-15s %-25s %-20s %-20s %-20s %-15s %-15s %-15s %-10s %-15s%n",
                i++,
                o.getId(),
                o.getTitle(),
                String.valueOf(o.getLevel()),
                o.getCompanyName(),
                String.valueOf(o.getPreferredMajor()),
                slotsStr,
                o.getOpenDate(),
                o.getCloseDate(),
                o.getStatus(),
                o.isVisible() ? "ON" : "OFF"
            );
        }

        System.out.println("\n(Total: " + myOpps.size() + " internship opportunities)\n");
    }

    private void viewAllApplications() {
        ConsoleUI.sectionHeader("Company Representative View > View All Internship Applications");
        System.out.println();

        List<Application> myApps = applicationService.getApplicationsByRepresentative(rep);

        if (myApps.isEmpty()) {
            System.out.println("✗ No applications found.\n");
            System.out.print("Press enter to return... ");
            sc.nextLine();
            ConsoleUI.sectionHeader("Company Representative View");
            return;
        }

        System.out.printf(
            "%-4s %-15s %-15s %-15s %-20s %-20s %-20s %-20 %-12s%n",
            "S/N", "Application ID", "Student ID", "Opportunity ID",
            "Internship Title", "Internship Level", "Company",
            "Status", "Applied At"
        );
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------------------------------");

        int i = 1;
        for (Application a : myApps) {
            InternshipOpportunity o = a.getOpportunity();

            System.out.printf(
                "%-4d %-15s %-15s %-15s %-20s %-20s %-20s %-20s %-12s%n",
                i++,
                a.getId(),
                a.getStudent().getId(),
                o.getId(),
                o.getTitle(),
                o.getLevel(),
                o.getCompanyName(),
                a.getStatus(),
                a.getAppliedAt()
            );
        }

        System.out.println("\n(Total: " + myApps.size() + " internship applications)\n");

        System.out.print("Press enter to return... ");
        sc.nextLine();
        ConsoleUI.sectionHeader("Company Representative View");
    }

    private void forceFirstTimePasswordChange() {
        System.out.println("\nYou are currently using the default password.");
        System.out.println("Please change your password before accessing the system.\n");

        while (rep.isFirstLogin()) {

            System.out.print("Enter new password: ");
            String newPwd = sc.nextLine().trim();

            System.out.print("Confirm new password: ");
            String confirm = sc.nextLine().trim();

            if (!newPwd.equals(confirm)) {
                System.out.println("✗ Passwords do not match. please try again.\n");
                continue;
            }

            if (!rep.forceFirstTimePasswordChange(newPwd)) {
                System.out.println("✗ Unable to change password. Please try again.\n");
                continue;
            }

            loader.saveUsers(users);
            System.out.println("\n✓ Password updated. You may now use the system.\n");
        }
    }

    /** 
     * @return Major
     */
    /* === helpers === */

    private Major selectMajor() {
        Major[] majors = Major.values();
        for (int i = 0; i < majors.length; i++) {
            System.out.printf("(%d) %s%n", i + 1, majors[i].name());
        }

        int choice = -1;
        while (choice < 1 || choice > majors.length) {
            System.out.print("Enter Preferred Major: ");
            String input = sc.nextLine().trim();
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("✗ Invalid input. Please enter a number.");
            }
        }

        return majors[choice - 1];
    }

    /** 
     * @return InternshipLevel
     */
    private InternshipLevel selectLevel() {
        InternshipLevel[] levels = InternshipLevel.values();
        for (int i = 0; i < levels.length; i++) {
            System.out.printf("(%d) %s%n", i + 1, levels[i].name());
        }

        int choice = -1;
        while (choice < 1 || choice > levels.length) {
            System.out.print("Enter Internship Level: ");
            String input = sc.nextLine().trim();
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("✗ Invalid input. please enter a number.");
            }
        }

        return levels[choice - 1];
    }

     /** 
      * @param max
      * @return int
      */
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