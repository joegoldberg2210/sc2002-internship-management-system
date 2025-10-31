package boundary;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.stream.Collectors;


import control.OpportunityService;
import entity.InternshipOpportunity;
import control.DataLoader;
import entity.CompanyRepresentative;
import entity.User;
import enumerations.InternshipLevel;
import ui.ConsoleUI;

public class CompanyRepView {
    private final Scanner sc;
    private final CompanyRepresentative rep;
    private final OpportunityService opportunityService;
    private final List<User> users;
    private final DataLoader loader;

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
            case "0" -> { return; }
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
            case "0" -> {return;}
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
            return;  // ✅ instead of manageAccount()
        } else {
            System.out.println("\n✗ Current password is incorrect.");
        }
    }

    private void manageOpportunities() {
    ConsoleUI.sectionHeader("Company Representative View > Manage Internship Opportunities");
    System.out.println("(1) Create Opportunity");
    System.out.println("(2) Edit Opportunity");
    System.out.println("(3) Delete Opportunity");
    System.out.println("(4) Toggle Visibility");
    System.out.println("(5) View My Opportunities");
    System.out.println("(0) Back to Main Menu");
    System.out.print("Enter choice: ");
    String c = sc.nextLine().trim();

    switch (c) {
        case "1" -> createOpportunity();
        case "2" -> editOpportunity();
        case "3" -> deleteOpportunity();
        case "4" -> toggleVisibility();
        case "5" -> viewMyOpportunities();
        case "0" -> { return; }
        default -> System.out.println("✗ Invalid choice.\n");
    }
    }


    private void reviewApplications() {
        ConsoleUI.sectionHeader("Company Representative View > Review Applications");
        System.out.println();
    }

    private void createOpportunity() {
    ConsoleUI.sectionHeader("Create New Internship Opportunity");

    System.out.print("Title: ");
    String title = sc.nextLine().trim();

    System.out.print("Description: ");
    String description = sc.nextLine().trim();

    System.out.print("Preferred Major: ");
    String major = sc.nextLine().trim();

    System.out.print("Level (BASIC / INTERMEDIATE / ADVANCED): ");
    InternshipLevel level = InternshipLevel.valueOf(sc.nextLine().trim().toUpperCase());

    System.out.print("Slots: ");
    int slots = Integer.parseInt(sc.nextLine().trim());

    InternshipOpportunity opp = new InternshipOpportunity(
        UUID.randomUUID().toString().substring(0, 6),
        title,
        description,
        level,
        major,
        LocalDate.now(),
        LocalDate.now().plusMonths(3),
        rep.getCompanyName(),
        slots,
        rep
    );

    opportunityService.createOpportunity(rep, opp);
    System.out.println("✓ Opportunity created and awaiting staff approval.\n");
    return;  // will go back to menu 

}

private void editOpportunity() {
    boolean continueEditing = true;

    while (continueEditing) {
        ConsoleUI.sectionHeader("Edit Existing Opportunity");

        System.out.print("Enter Opportunity ID: ");
        String id = sc.nextLine().trim();
        InternshipOpportunity existing = opportunityService.findById(id);

        if (existing == null) {
            System.out.println("✗ Opportunity not found.");
        } else if (!rep.equals(existing.getRepInCharge())) {
            System.out.println("✗ You may only edit your own opportunities.");
        } else {
            boolean editing = true;
            while (editing) {
                System.out.println("\n--- Current Details ---");
                System.out.println("1. Title           : " + existing.getTitle());
                System.out.println("2. Description     : " + existing.getDescription());
                System.out.println("3. Preferred Major : " + existing.getPreferredMajor());
                System.out.println("4. Level           : " + existing.getLevel());
                System.out.println("5. Slots           : " + existing.getSlots());
                System.out.println("6. Open Date       : " + existing.getOpenDate());
                System.out.println("7. Close Date      : " + existing.getCloseDate());
                System.out.println("0. Done editing");
                System.out.print("\nEnter number to edit: ");

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
                        System.out.print("Enter new Preferred Major: ");
                        existing.setPreferredMajor(sc.nextLine().trim());
                    }
                    case "4" -> {
                        System.out.print("Enter new Level (BASIC / INTERMEDIATE / ADVANCED): ");
                        try {
                            existing.setLevel(InternshipLevel.valueOf(sc.nextLine().trim().toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            System.out.println("✗ Invalid level. Keeping old value.");
                        }
                    }
                    case "5" -> {
                        System.out.print("Enter new number of slots: ");
                        try {
                            existing.setSlots(Integer.parseInt(sc.nextLine().trim()));
                        } catch (NumberFormatException e) {
                            System.out.println("✗ Invalid number. Keeping old value.");
                        }
                    }
                    case "6" -> {
                        System.out.print("Enter new Open Date (YYYY-MM-DD): ");
                        try {
                            existing.setOpenDate(LocalDate.parse(sc.nextLine().trim()));
                        } catch (Exception e) {
                            System.out.println("✗ Invalid date format. Keeping old value.");
                        }
                    }
                    case "7" -> {
                        System.out.print("Enter new Close Date (YYYY-MM-DD): ");
                        try {
                            existing.setCloseDate(LocalDate.parse(sc.nextLine().trim()));
                        } catch (Exception e) {
                            System.out.println("✗ Invalid date format. Keeping old value.");
                        }
                    }
                    case "0" -> {
                        editing = false;
                        opportunityService.editOpportunity(rep, existing);
                        System.out.println("✓ All edits saved and sent for re-approval.");
                    }
                    default -> System.out.println("✗ Invalid choice, try again.");
                }
            }
        }

        System.out.print("\nDo you want to edit another opportunity? (Y/N): ");
        String again = sc.nextLine().trim().toUpperCase();
        continueEditing = again.equals("Y");
    }
}


private void deleteOpportunity() {
    ConsoleUI.sectionHeader("Delete Opportunity");
    System.out.print("Enter Opportunity ID: ");
    String id = sc.nextLine().trim();
    opportunityService.deleteOpportunity(rep, id);
}

private void toggleVisibility() {
    ConsoleUI.sectionHeader("Toggle Opportunity Visibility");
    System.out.print("Enter Opportunity ID: ");
    String id = sc.nextLine().trim();
    InternshipOpportunity opp = opportunityService.findById(id);
    if (opp == null) {
        System.out.println("✗ Not found.");
        return;
    }

    boolean newState = !opp.isVisible();
    opportunityService.toggleVisibility(opp, newState);
}

    private void viewMyOpportunities() {
    ConsoleUI.sectionHeader("My Internship Opportunities");
    List<InternshipOpportunity> myOpps = opportunityService.getByCompany(rep.getCompanyName());

    if (myOpps.isEmpty()) {
        System.out.println("✗ No opportunities found.\n");
        return;
    }

    for (InternshipOpportunity opp : myOpps) {
        System.out.printf("ID: %-6s | Title: %-20s | Level: %-12s | Slots: %-3d | Visible: %s%n",
                opp.getId(), opp.getTitle(), opp.getLevel(), opp.getSlots(),
                opp.isVisible() ? "Yes" : "No");
    }
    System.out.println();
}
}