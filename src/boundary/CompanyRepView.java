package boundary;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import control.OpportunityService;
import control.DataLoader;
import entity.CompanyRepresentative;
import entity.InternshipOpportunity;
import entity.User;
import enumerations.InternshipLevel;
import enumerations.Major;
import enumerations.OpportunityStatus;
import ui.ConsoleUI;

public class CompanyRepView {
    private final Scanner sc;
    private final CompanyRepresentative rep;
    private final OpportunityService opportunityService;
    private final List<User> users;
    private final DataLoader loader;

    // constructor 
    public CompanyRepView(Scanner sc, CompanyRepresentative rep, List<User> users, DataLoader loader, OpportunityService opportunityService) {
        this.sc = sc;
        this.rep = rep;
        this.users = users;
        this.loader = loader;
        this.opportunityService = opportunityService;
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
        System.out.println("(3) Review Applications");
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
            case "0" -> ConsoleUI.sectionHeader("Company Representative View > Manage Account");
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

        boolean successful = newPwd.equals(confirm) && rep.changePassword(current, newPwd);

        if (successful) {
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
        System.out.println("(2) Edit Existing Internship Opportunity");
        System.out.println("(3) Delete Opportunity");
        System.out.println("(4) Toggle Visibility");
        System.out.println("(5) View My Opportunities");
        System.out.println("(0) Back to Company Representative View");
        System.out.println();
        System.out.print("Enter choice: ");
        String c = sc.nextLine().trim();

        switch (c) {
            case "1" -> createOpportunity();
            case "2" -> editOpportunity();
            case "3" -> deleteOpportunity();
            case "4" -> toggleVisibility();
            case "5" -> viewMyOpportunities();
            case "0" -> { ConsoleUI.sectionHeader("Company Representative View"); }
            default -> System.out.println("✗ Invalid choice.\n");
        }
    }

    private void reviewApplications() {
        ConsoleUI.sectionHeader("Company Representative View > Review Applications");
        System.out.println();
    }

    private void createOpportunity() {
        ConsoleUI.sectionHeader("Company Representative View > Create New Internship Opportunity");
        
        long activeCount = opportunityService.getByCompany(rep.getCompanyName()).size();
        if (activeCount >= 5) {
            System.out.println("✗ You are unable to create new internship opportunity as you already have 5 active opportunities.\n");
            System.out.print("Press enter key to continue... ");
            sc.nextLine(); 
            ConsoleUI.sectionHeader("Company Representative View");
        }

        System.out.print("Title: ");
        String title = sc.nextLine().trim();

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

        // create and persist
        opportunityService.createOpportunity(rep, opp);

        ConsoleUI.sectionHeader("Company Representative View");
    }

    private void editOpportunity() {
        boolean continueEditing = true;

        while (continueEditing) {
            ConsoleUI.sectionHeader("Company Representative View > Edit Existing Internship Opportunity");

            System.out.print("Enter Opportunity ID to edit: ");
            String id = sc.nextLine().trim();
            InternshipOpportunity existing = opportunityService.findById(id);

            if (existing == null) {
                System.out.println("✗ Opportunity not found.");
            } else if (!rep.equals(existing.getRepInCharge())) {
                System.out.println("✗ You may only edit your own opportunities.");
            } else if (existing.getStatus() != OpportunityStatus.PENDING) {
                System.out.println("✗ You cannot edit an opportunity that has already been approved by Career Center Staff."); 
            } else {
                boolean editing = true;
                while (editing) {
                    System.out.println("\n----- Details of Internship Opportunity -----");
                    System.out.println();
                    System.out.println("(1) Title           : " + existing.getTitle());
                    System.out.println("(2) Description     : " + existing.getDescription());
                    System.out.println("(3) Major           : " + existing.getPreferredMajor());
                    System.out.println("(4) Level           : " + existing.getLevel());
                    System.out.println("(5) Slots           : " + existing.getSlots());
                    System.out.println("(6) Open Date       : " + existing.getOpenDate());
                    System.out.println("(7) Close Date      : " + existing.getCloseDate());
                    System.out.println();
                    System.out.print("Choose a field number to edit, or 0 to go back: ");

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
                            Major m = selectMajor();
                            existing.setPreferredMajor(m);
                        }
                        case "4" -> {
                            InternshipLevel l = selectLevel();
                            existing.setLevel(l);
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
            }

            System.out.print("\nDo you want to edit another internship opportunity? (y/n): ");
            String again = sc.nextLine().trim().toUpperCase();
            continueEditing = again.equals("Y");
        }
    }

    private void deleteOpportunity() {
        boolean continueDeleting = true;

        while (continueDeleting) {
            ConsoleUI.sectionHeader("Company Representative View > Delete Internship Opportunity");

            System.out.print("Enter opportunity id to delete: ");
            String raw = sc.nextLine();
            String id = (raw == null) ? "" : raw.trim();

            if (id.isEmpty()) {
                System.out.println("✗ Invalid ID.");
            } else {
                InternshipOpportunity existing = opportunityService.findById(id);

                if (existing == null) {
                    System.out.println("✗ Opportunity not found.");
                } else if (!rep.equals(existing.getRepInCharge())) {
                    System.out.println("✗ You may only delete your own opportunities.");
                } else if (existing.getStatus() != OpportunityStatus.PENDING) {
                    System.out.println("✗ You can only delete opportunities that are still pending approval.");
                } else {
                    opportunityService.deleteOpportunity(rep, id);
                }
            }

            System.out.print("\nDo you want to delete another internship opportunity? (y/n): ");
            String again = sc.nextLine().trim().toUpperCase();
            continueDeleting = again.equals("Y");
        }
    }

    private void toggleVisibility() {
        ConsoleUI.sectionHeader("Toggle Opportunity Visibility");
        System.out.print("Enter Opportunity ID: ");
        String id = sc.nextLine().trim();
        InternshipOpportunity opp = opportunityService.findById(id);
        if (opp == null) {
            System.out.println("✗ Not found.");
            ConsoleUI.sectionHeader("Company Representative View");
        }

        boolean newState = !opp.isVisible();
        opportunityService.toggleVisibility(opp, newState);
    }

    private void viewMyOpportunities() {
        ConsoleUI.sectionHeader("Company Representative View > View My Internship Opportunities");
        List<InternshipOpportunity> myOpps = opportunityService.getByCompany(rep.getCompanyName());

        if (myOpps.isEmpty()) {
            System.out.println("✗ No opportunities found.\n");
            ConsoleUI.sectionHeader("Company Representative View");
        }

        System.out.println();   
        System.out.printf(
            "%-4s %-15s %-25s %-12s %-15s %-9s %-10s %-10s %-15s %-12s%n",
            "S/N", "ID", "Title", "Major", "Level", "Slots", "Status", "Visible", "Company", "Opp Status"
        );
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------");

        int i = 1;
        for (InternshipOpportunity opp : myOpps) {
            String slotsStr = String.format("%d/%d", opp.getConfirmedSlots(), opp.getSlots());

            System.out.printf(
                "%-4d %-15s %-25s %-12s %-15s %-9s %-10s %-10s %-15s %-12s%n",
                i++,
                opp.getId(),
                opp.getTitle(),
                String.valueOf(opp.getPreferredMajor()),
                String.valueOf(opp.getLevel()),
                slotsStr,
                String.valueOf(opp.getStatus()),
                opp.isVisible() ? "yes" : "no",
                opp.getCompanyName(),
                String.valueOf(opp.getStatus())  // newly added column for opportunity status
            );
        }

        System.out.println("\n(Total: " + myOpps.size() + " internship opportunities listed)");
        System.out.println();

        System.out.print("Press enter key to continue... ");
        sc.nextLine(); 
        ConsoleUI.sectionHeader("Company Representative View");
    }

    /* === helpers === */

    private Major selectMajor() {
        Major[] majors = Major.values();
        for (int i = 0; i < majors.length; i++) {
            System.out.printf("(%d) %s%n", i + 1, majors[i].name());
        }

        int choice = -1;
        while (choice < 1 || choice > majors.length) {
            System.out.print("Enter preferred major: ");
            String input = sc.nextLine().trim();
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("✗ Invalid input. Please enter a number.");
            }
        }

        return majors[choice - 1];
    }

    private InternshipLevel selectLevel() {
        InternshipLevel[] levels = InternshipLevel.values();
        for (int i = 0; i < levels.length; i++) {
            System.out.printf("(%d) %s%n", i + 1, levels[i].name());
        }

        int choice = -1;
        while (choice < 1 || choice > levels.length) {
            System.out.print("Enter internship level: ");
            String input = sc.nextLine().trim();
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("✗ Invalid input. please enter a number.");
            }
        }

        return levels[choice - 1];
    }
}