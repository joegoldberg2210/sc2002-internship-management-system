package boundary;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import control.AccountApprovalService;
import control.DataLoader;
import control.ApplicationService;
import control.OpportunityService;
import entity.CareerCenterStaff;
import entity.CompanyRepresentative;
import entity.FilterCriteria;
import entity.InternshipOpportunity;
import entity.Report;
import entity.User;
import entity.WithdrawalRequest;
import enumerations.InternshipLevel;
import enumerations.Major;
import enumerations.OpportunityStatus;
import ui.ConsoleUI;

public class CareerCenterStaffView {
    private final Scanner sc;
    private final CareerCenterStaff staff;
    private final List<User> users;
    private final DataLoader loader;
    private final AccountApprovalService approval;
    private final OpportunityService oppService;
    private final ApplicationService applicationService;

    // persistent filter + sort for "view all internship opportunities"
    private final FilterCriteria allOppFilter = new FilterCriteria();
    private String allOppSortKey = "title";      // "title", "company", "slots", "openDate", "closeDate"
    private boolean allOppSortDescending = false;

    public CareerCenterStaffView(Scanner sc, CareerCenterStaff staff, List<User> users, DataLoader loader, AccountApprovalService approval, OpportunityService oppService, ApplicationService applicationService) {
        this.sc = sc;
        this.staff = staff;
        this.users = users;
        this.loader = loader;
        this.approval = approval;
        this.oppService = oppService;
        this.applicationService = applicationService;
    }

    public void run() {
        ConsoleUI.sectionHeader("Career Center Staff View");

        while (staff.isFirstLogin()) {
            forceFirstTimePasswordChange();
        }

        boolean running = true;
        while (running) {
            showMenu();
            System.out.print("Enter choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> manageAccount();
                case "2" -> reviewPendingRegistrations();
                case "3" -> reviewPendingInternshipOpportunities();
                case "4" -> reviewPendingWithdrawalRequests();
                case "5" -> viewAllRegistrations();
                case "6" -> viewAllInternshipOpportunities();
                case "7" -> viewAllWithdrawalRequests();
                case "8" -> generateReports();
                case "logout" -> {
                    System.out.println("\n✓ You have logged out of your account.\n");
                    return;
                }
                default -> System.out.println("✗ Invalid choice, please try again.\n");
            }
        }
 
        System.out.println();
        System.out.println("✓ You have logged out of the system.");
        System.out.println();
    }

    private void showMenu() {
        System.out.println("(1) Manage Account");
        System.out.println("(2) Review Pending Company Representative Registrations");
        System.out.println("(3) Review Pending Internship Opportunities");
        System.out.println("(4) Review Pending Withdrawal Requests");
        System.out.println("(5) View All Company Representative Registrations");   
        System.out.println("(6) View All Internship Opportunities");
        System.out.println("(7) View All Withdrawal Requests");
        System.out.println("(8) Generate Internship Opportunities Report");
        System.out.println();
        System.out.println("→ Type 'logout' here to logout");
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
            case "0" -> { ConsoleUI.sectionHeader("Career Center Staff View"); }
            default -> { ConsoleUI.sectionHeader("Student View"); }
        }
    }

    private void viewProfile() {
        ConsoleUI.sectionHeader("Career Center Staff View > Manage Account > View Profile");
        System.out.println("ID         : " + staff.getId());
        System.out.println("Name       : " + staff.getName());
        System.out.println("Department : " + staff.getStaffDepartment());
        System.out.println();
        System.out.print("Press enter key to continue... ");
        sc.nextLine();
        ConsoleUI.sectionHeader("Career Center Staff View");
    }

    private void changePassword() {
        ConsoleUI.sectionHeader("Career Center Staff View > Manage Account > Change Password");

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

       boolean successful = newPwd.equals(confirm) && staff.changePassword(current, newPwd);

        if (successful) {
            staff.setFirstLogin(false);
            System.out.println("\n✓ Password changed successfully!");
            loader.saveUsers(users);
            ConsoleUI.sectionHeader("Career Center Staff View");
        } else {
            System.out.println("\n✗ Unable to change password. Please try again.");
            ConsoleUI.sectionHeader("Career Center Staff View");
        }
    }

    private void reviewPendingRegistrations() {
        ConsoleUI.sectionHeader("Career Center Staff View > Review Pending Company Representative Registrations");

        List<CompanyRepresentative> pending = approval.getPendingCompanyReps();
        if (pending == null || pending.isEmpty()) {
            System.out.println("✗ No pending company representative registrations.\n");
            System.out.print("Press enter to continue... ");
            sc.nextLine();
            ConsoleUI.sectionHeader("Career Center Staff View");
            return;
        }

        // table header
        System.out.println();
        System.out.printf(
            "%-4s %-28s %-25s %-25s %-20s %-20s%n",
            "S/N", "Company Representative ID", "Name", "Company", "Department", "Position"
        );
        System.out.println("--------------------------------------------------------------------------------------------------------------------");

        int i = 1;
        for (CompanyRepresentative r : pending) {
            System.out.printf(
                "%-4d %-28s %-25s %-25s %-20s %-20s%n",
                i++,
                r.getId(),
                r.getName(),
                r.getCompanyName(),
                r.getDepartment(),
                r.getPosition()
            );
        }

        System.out.println("\n(Total: " + pending.size() + " pending company representative applications)\n");

        CompanyRepresentative selected = null;

        while (true) {
            System.out.print("Enter Company Representative ID to review (blank to cancel): ");
            String id = sc.nextLine().trim();

            if (id.isEmpty()) {
                System.out.println("Review cancelled.\n");
                ConsoleUI.sectionHeader("Career Center Staff View");
                return;
            }

            selected = pending.stream()
                    .filter(r -> r.getId().equalsIgnoreCase(id))
                    .findFirst()
                    .orElse(null);

            if (selected != null) break;

            System.out.println("✗ Invalid Company Representative ID. Please try again.\n");
        }

        // detail block
        System.out.println("\n────────────────────────────────────────────────────────────");
        System.out.println("          Company Representative Application Details        ");
        System.out.println("────────────────────────────────────────────────────────────");
        System.out.printf("%-25s: %s%n", "Name", selected.getName());
        System.out.printf("%-25s: %s%n", "Company Representative ID", selected.getId());
        System.out.printf("%-25s: %s%n", "Company", selected.getCompanyName());
        System.out.printf("%-25s: %s%n", "Department", selected.getDepartment());
        System.out.printf("%-25s: %s%n", "Position", selected.getPosition());
        System.out.println("────────────────────────────────────────────────────────────\n");

        // action menu
        System.out.print("\nApprove this company representative application? (y/n): ");
        String decision = sc.nextLine().trim().toLowerCase();

        boolean approve = decision.equals("y") || decision.equals("yes");
        if (approve) {
            approval.approveCompanyRep(staff, selected);
        } else {
            approval.rejectCompanyRep(staff, selected);
        }

        System.out.print("Press enter to return... ");
        sc.nextLine();
        ConsoleUI.sectionHeader("Career Center Staff View");
    }

    private void reviewPendingInternshipOpportunities() {
        ConsoleUI.sectionHeader("Career Center Staff View > Review Pending Internship Opportunities");

        List<InternshipOpportunity> pending = oppService.getPending();
        if (pending.isEmpty()) {
            System.out.println("✗ No pending internship opportunities.\n");
            ConsoleUI.sectionHeader("Career Center Staff View");
            return;
        }

        // table header
        System.out.println();
        System.out.printf(
            "%-4s %-15s %-25s %-20s %-20s %-15s %-20s %-12s %-16s%n",
            "S/N", "Opportunity ID", "Internship Title", "Preferred Major", "Internship Level", "Total Slots", "Company", "Open Date", "Closing Date"
        );
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------------------");

        int i = 1;
        for (InternshipOpportunity o : pending) {
            System.out.printf(
                "%-4d %-15s %-25s %-20s %-20s %-15s %-20s %-12s %-16s%n",
                i++,
                o.getId(),
                o.getTitle(),
                String.valueOf(o.getPreferredMajor()),
                String.valueOf(o.getLevel()),
                o.getSlots(),
                o.getCompanyName(),
                String.valueOf(o.getOpenDate()),
                String.valueOf(o.getCloseDate())
            );
        }

        System.out.println("\n(Total: " + pending.size() + " pending internship opportunities)\n");

        InternshipOpportunity selected = null;

        while (true) {
            System.out.println();
            System.out.print("Enter Opportunity ID to review (blank to cancel): ");
            String id = sc.nextLine().trim();

            if (id.isEmpty()) {
                System.out.println("Review cancelled.\n");
                ConsoleUI.sectionHeader("Career Center Staff View");
                return;
            }

            selected = pending.stream()
                    .filter(o -> o.getId().equalsIgnoreCase(id))
                    .findFirst()
                    .orElse(null);

            if (selected != null) break; // success → exit loop

            System.out.println("✗ Invalid Opportunity ID. Please try again.\n");
        }

        // detail view
        System.out.println();
        System.out.println("\n────────────────────────────────────────────────────────────");
        System.out.println("                 Internship Opportunity Details              ");
        System.out.println("────────────────────────────────────────────────────────────");
        System.out.printf("%-18s: %s%n", "Opportunity ID", selected.getId());
        System.out.printf("%-18s: %s%n", "Internship Title", selected.getTitle());
        System.out.printf("%-18s: %s%n", "Company", selected.getCompanyName());
        System.out.printf("%-18s: %s%n", "Preferred Major", selected.getPreferredMajor());
        System.out.printf("%-18s: %s%n", "Internship Level", selected.getLevel());
        System.out.printf("%-18s: %s%n", "Open Date", selected.getOpenDate());
        System.out.printf("%-18s: %s%n", "Close Date", selected.getCloseDate());
        System.out.printf("%-18s: %d/%d%n", "Slots", selected.getConfirmedSlots(), selected.getSlots());
        System.out.printf("%-18s: %s%n", "Status", selected.getStatus());
        System.out.println("────────────────────────────────────────────────────────────\n");

        System.out.print("\nApprove this internship opportunity? (y/n): ");
        String decision = sc.nextLine().trim().toLowerCase();

        boolean approve = decision.equals("y") || decision.equals("yes");
        if (approve) {
            oppService.approveOpportunity(staff, selected);
        } else {
            oppService.rejectOpportunity(staff, selected);
        }

        System.out.println();
        System.out.print("Press enter to return... ");
        sc.nextLine();
        ConsoleUI.sectionHeader("Career Center Staff View"); 
    }

    private void viewAllRegistrations() {
        ConsoleUI.sectionHeader("Career Center Staff View > View All Company Representative Registrations");

        // get all company representatives
        List<CompanyRepresentative> allReps = approval.getAllCompanyReps();

        if (allReps == null || allReps.isEmpty()) {
            System.out.println("✗ No company representative registrations found.\n");
            System.out.print("Press enter to continue... ");
            sc.nextLine();
            ConsoleUI.sectionHeader("Career Center Staff View");
            return;
        }

        // table header
        System.out.println();
        System.out.printf(
            "%-4s %-40s %-25s %-25s %-25s %-25s %-15s%n",
            "S/N", "Company Representative ID", "Name", "Company", "Department", "Position", "Status"
        );
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------");

        int i = 1;
        for (CompanyRepresentative r : allReps) {
            System.out.printf(
                "%-4d %-40s %-25s %-25s %-25s %-25s %-15s%n",
                i++,
                r.getId(),
                r.getName(),
                r.getCompanyName(),
                r.getDepartment(),
                r.getPosition(),
                r.getStatus()
            );
        }

        System.out.println("\n(Total: " + allReps.size() + " company representative registrations)\n");

        System.out.print("Press enter to return... ");
        sc.nextLine();
        ConsoleUI.sectionHeader("Career Center Staff View");
    }

    /** view all pending withdrawal requests and approve/reject them */
    private void reviewPendingWithdrawalRequests() {
        ConsoleUI.sectionHeader("Career Center Staff > View Pending Withdrawal Requests");

        List<WithdrawalRequest> pending = applicationService.getPendingWithdrawalRequests();

        if (pending.isEmpty()) {
            System.out.println("✓ No pending withdrawal requests.\n");
            System.out.print("Press enter to return... "); sc.nextLine();
            return;
        }

        System.out.printf("%-4s %-10s %-15s %-15s %-15s %-20s %-15s %-20s%n",
                "S/N", "Request ID", "Application ID", "Student ID", "Opportunity ID", "Internship Title", "Company", "Requested At");
        System.out.println("---------------------------------------------------------------------------------------------------------------------------");

        int i = 1;
        for (WithdrawalRequest req : pending) {
            System.out.printf("%-4d %-10s %-15s %-15s %-15s %-20s %-15s %-20s%n",
                    i++,
                    req.getId(),
                    req.getApplication().getId(),
                    req.getRequestedBy().getId(),
                    req.getApplication().getOpportunity().getId(),
                    req.getApplication().getOpportunity().getTitle(),
                    req.getApplication().getOpportunity().getCompanyName(),
                    req.getRequestedAt());
        }

        System.out.println("\n(Total: " + pending.size() + " pending withdrawal requests)\n");

        WithdrawalRequest selected = null;

        while (true) {
            System.out.println();
            System.out.print("Enter Request ID to review (blank to cancel): ");
            String id = sc.nextLine().trim();

            if (id.isEmpty()) {
                System.out.println("Request cancelled.\n");
                ConsoleUI.sectionHeader("Career Center Staff View");
                return;
            }

            selected = pending.stream()
                    .filter(r -> r.getId().equalsIgnoreCase(id))
                    .findFirst()
                    .orElse(null);

            if (selected != null) break;

            System.out.println("✗ Invalid Request ID. Please try again.\n");
        }

        // print request details
        System.out.println("\n────────────────────────────────────────────────────────────");
        System.out.println("                Withdrawal Request Details                  ");
        System.out.println("────────────────────────────────────────────────────────────");
        System.out.printf("%-18s: %s%n", "Request ID", selected.getId());
        System.out.printf("%-18s: %s%n", "Student ID", selected.getRequestedBy().getId());
        System.out.printf("%-18s: %s%n", "Application ID", selected.getApplication().getId());
        System.out.printf("%-18s: %s%n", "Internship Title", selected.getApplication().getOpportunity().getTitle());
        System.out.printf("%-18s: %s%n", "Internship Level", selected.getApplication().getOpportunity().getLevel());
        System.out.printf("%-18s: %s%n", "Company", selected.getApplication().getOpportunity().getCompanyName());
        System.out.printf("%-18s: %s%n", "Preferred Major", selected.getApplication().getOpportunity().getPreferredMajor());
        System.out.printf("%-18s: %s%n", "Requested At", selected.getRequestedAt());
        System.out.println("────────────────────────────────────────────────────────────");

        System.out.print("\nApprove this withdrawal request? (y/n): ");
        String decision = sc.nextLine().trim().toLowerCase();

        boolean approve = decision.equals("y") || decision.equals("yes");
        applicationService.reviewWithdrawalRequest(staff, selected, approve);

        System.out.println();
        System.out.print("Press enter to return... "); sc.nextLine();
        ConsoleUI.sectionHeader("Career Center Staff View");
    }

    private void viewAllInternshipOpportunities() {
        ConsoleUI.sectionHeader("Career Center Staff View > View All Internship Opportunities");

        while (true) {
            List<InternshipOpportunity> all = oppService.getAllOpportunities();

            if (all == null || all.isEmpty()) {
                System.out.println("✗ No internship opportunities found.\n");
                System.out.print("Press enter to return... ");
                sc.nextLine();
                ConsoleUI.sectionHeader("Career Center Staff View");
                return;
            }

            // apply filters using allOppFilter (status, level, preferred major, company)
            List<InternshipOpportunity> filtered = all.stream()
                    .filter(o -> allOppFilter.getStatus() == null
                            || o.getStatus() == allOppFilter.getStatus())
                    .filter(o -> allOppFilter.getLevel() == null
                            || o.getLevel() == allOppFilter.getLevel())
                    .filter(o -> allOppFilter.getPreferredMajor() == null
                            || o.getPreferredMajor().name()
                                    .equalsIgnoreCase(allOppFilter.getPreferredMajor()))
                    .filter(o -> allOppFilter.getCompany() == null
                            || o.getCompanyName().toLowerCase()
                                    .contains(allOppFilter.getCompany().toLowerCase()))
                    .collect(Collectors.toList());

            // apply sorting
            Comparator<InternshipOpportunity> cmp;
            if ("company".equalsIgnoreCase(allOppSortKey)) {
                cmp = Comparator.comparing(InternshipOpportunity::getCompanyName,
                        String.CASE_INSENSITIVE_ORDER);
            } else if ("slots".equalsIgnoreCase(allOppSortKey)) {
                cmp = Comparator.comparingInt(InternshipOpportunity::getSlots);
            } else if ("openDate".equalsIgnoreCase(allOppSortKey)) {
                cmp = Comparator.comparing(InternshipOpportunity::getOpenDate);
            } else if ("closeDate".equalsIgnoreCase(allOppSortKey)) {
                cmp = Comparator.comparing(InternshipOpportunity::getCloseDate);
            } else {
                cmp = Comparator.comparing(InternshipOpportunity::getTitle,
                        String.CASE_INSENSITIVE_ORDER);
                allOppSortKey = "title";
            }
            if (allOppSortDescending) {
                cmp = cmp.reversed();
            }
            filtered.sort(cmp);

            // show current filter + sort + table
            printAllOppFilterAndSort(allOppFilter, allOppSortKey, allOppSortDescending);
            printAllOpportunitiesTable(filtered);

            System.out.println("(1) Edit Filter");
            System.out.println("(2) Edit Sort");
            System.out.println("(0) Reset Filter & Sort");
            System.out.print("Enter choice (blank to cancel): ");
            String choice = sc.nextLine().trim();

            if (choice.isEmpty()) {
                ConsoleUI.sectionHeader("Career Center Staff View");
                return;
            } else if ("1".equals(choice)) {
                // edit filter menu
                while (true) {
                    System.out.println();
                    System.out.println("Filter opportunities by:");
                    System.out.println("(1) Internship Level");
                    System.out.println("(2) Opportunity Status");
                    System.out.println("(3) Preferred Major");
                    System.out.println("(4) Company");
                    System.out.print("Enter choice (blank to cancel): ");
                    String f = sc.nextLine().trim();

                    if (f.isEmpty()) {
                        break;
                    }

                    // ---- internship level ----
                    if ("1".equals(f)) {
                        System.out.println();
                        System.out.println("Select Internship Level:");
                        System.out.println("(1) Basic");
                        System.out.println("(2) Intermediate");
                        System.out.println("(3) Advanced");
                        System.out.print("Enter choice (blank to cancel): ");
                        String lv = sc.nextLine().trim();

                        switch (lv) {
                            case "1":
                                allOppFilter.setLevel(InternshipLevel.BASIC);
                                System.out.println("✓ Internship level filter set to BASIC.\n");
                                break;
                            case "2":
                                allOppFilter.setLevel(InternshipLevel.INTERMEDIATE);
                                System.out.println("✓ Internship level filter set to INTERMEDIATE.\n");
                                break;
                            case "3":
                                allOppFilter.setLevel(InternshipLevel.ADVANCED);
                                System.out.println("✓ Internship level filter set to ADVANCED.\n");
                                break;
                            case "":
                                System.out.println("Internship level filter unchanged.\n");
                                break;
                            default:
                                System.out.println("✗ Invalid choice.\n");
                                break;
                        }
                        break;
                    }

                    // ---- opportunity status ----
                    else if ("2".equals(f)) {
                        System.out.println();
                        System.out.println("Select opportunity status:");
                        System.out.println("(1) Pending");
                        System.out.println("(2) Approved");
                        System.out.println("(3) Filled");
                        System.out.println("(4) Rejected");
                        System.out.print("Enter choice (blank to cancel): ");
                        String sf = sc.nextLine().trim();

                        switch (sf) {
                            case "1":
                                allOppFilter.setStatus(OpportunityStatus.PENDING);
                                System.out.println("✓ Status filter set to PENDING.\n");
                                break;
                            case "2":
                                allOppFilter.setStatus(OpportunityStatus.APPROVED);
                                System.out.println("✓ Status filter set to APPROVED.\n");
                                break;
                            case "3":
                                allOppFilter.setStatus(OpportunityStatus.FILLED);
                                System.out.println("✓ Status filter set to FILLED.\n");
                                break;
                            case "4":
                                allOppFilter.setStatus(OpportunityStatus.REJECTED);
                                System.out.println("✓ Status filter set to REJECTED.\n");
                                break;
                            case "":
                                System.out.println("Opportunity status filter unchanged.\n");
                                break;
                            default:
                                System.out.println("✗ Invalid choice.\n");
                                break;
                        }
                        break;
                    }

                    // ---- preferred major ----
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
                            case "1":
                                allOppFilter.setPreferredMajor("CSC");
                                System.out.println("✓ Preferred major filter set to CSC.\n");
                                break;
                            case "2":
                                allOppFilter.setPreferredMajor("DSAI");
                                System.out.println("✓ Preferred major filter set to DSAI.\n");
                                break;
                            case "3":
                                allOppFilter.setPreferredMajor("CEG");
                                System.out.println("✓ Preferred major filter set to CEG.\n");
                                break;
                            case "4":
                                allOppFilter.setPreferredMajor("IEM");
                                System.out.println("✓ Preferred major filter set to IEM.\n");
                                break;
                            case "5":
                                allOppFilter.setPreferredMajor("BCG");
                                System.out.println("✓ Preferred major filter set to BCG.\n");
                                break;
                            case "6":
                                allOppFilter.setPreferredMajor("BCE");
                                System.out.println("✓ Preferred major filter set to BCE.\n");
                                break;
                            case "":
                                System.out.println("Preferred major filter unchanged.\n");
                                break;
                            default:
                                System.out.println("✗ Invalid choice.\n");
                                break;
                        }
                        break;
                    }

                    // ---- company name ----
                    else if ("4".equals(f)) {
                        System.out.println();
                        System.out.print("Enter company keyword (blank to cancel): ");
                        String kw = sc.nextLine().trim();

                        if (kw.isEmpty()) {
                            System.out.println("Company filter unchanged.\n");
                        } else {
                            allOppFilter.setCompany(kw);
                            System.out.println("✓ Company filter updated.\n");
                        }
                        break;
                    }
                }

            } else if ("2".equals(choice)) {
                // edit sort
                System.out.println();
                System.out.println("Sort opportunities by:");
                System.out.println("(1) Internship title");
                System.out.println("(2) Company");
                System.out.println("(3) Number of Slots");
                System.out.println("(4) Open Date");
                System.out.println("(5) Close Date");
                System.out.print("Enter choice (blank to cancel): ");
                String s = sc.nextLine().trim();

                if (!s.isEmpty()) {
                    if ("1".equals(s)) {
                        allOppSortKey = "title";
                    } else if ("2".equals(s)) {
                        allOppSortKey = "company";
                    } else if ("3".equals(s)) {
                        allOppSortKey = "slots";
                    } else if ("4".equals(s)) {
                        allOppSortKey = "openDate";
                    } else if ("5".equals(s)) {
                        allOppSortKey = "closeDate";
                    } else {
                        System.out.println("✗ Invalid choice. Keeping previous sorting.\n");
                    }

                    if ("1".equals(s) || "2".equals(s) || "3".equals(s)
                            || "4".equals(s) || "5".equals(s)) {
                        System.out.println();
                        System.out.println("Sort order:");
                        System.out.println("(1) Ascending");
                        System.out.println("(2) Descending");
                        System.out.print("Enter choice (blank = ascending): ");
                        String order = sc.nextLine().trim();

                        if (order.isEmpty() || "1".equals(order)) {
                            allOppSortDescending = false;
                        } else if ("2".equals(order)) {
                            allOppSortDescending = true;
                        } else {
                            System.out.println("✗ Invalid choice. Keeping previous order.\n");
                        }
                    }
                }

            } else if ("0".equals(choice)) {
                // reset filter & sort
                allOppFilter.setStatus(null);
                allOppFilter.setLevel(null);
                allOppFilter.setPreferredMajor(null);
                allOppFilter.setCompany(null);
                allOppFilter.setClosingDateBefore(null);

                allOppSortKey = "title";
                allOppSortDescending = false;

                System.out.println("\n✓ Filters and sorting reset to default.\n");
            } else {
                System.out.println("✗ Invalid choice. Please try again.\n");
            }
        }
    }

    private void printAllOppFilterAndSort(FilterCriteria filter,
                                        String sortKey,
                                        boolean sortDescending) {
        StringBuilder fb = new StringBuilder("Current Filters: ");
        boolean any = false;

        if (filter.getStatus() != null) {
            fb.append("Status = ")
            .append(filter.getStatus().name().toLowerCase())
            .append("  ");
            any = true;
        }
        if (filter.getLevel() != null) {
            fb.append("Internship Level = ")
            .append(filter.getLevel().name().toLowerCase())
            .append("  ");
            any = true;
        }
        if (filter.getPreferredMajor() != null && !filter.getPreferredMajor().isEmpty()) {
            fb.append("Preferred Major = ")
            .append(filter.getPreferredMajor().toLowerCase())
            .append("  ");
            any = true;
        }
        if (filter.getCompany() != null && !filter.getCompany().trim().isEmpty()) {
            fb.append("Company contains \"")
            .append(filter.getCompany())
            .append("\"  ");
            any = true;
        }

        if (!any) {
            fb = new StringBuilder("Current filters: none");
        }

        String sortLabel;
        if ("company".equalsIgnoreCase(sortKey)) {
            sortLabel = "Company";
        } else if ("slots".equalsIgnoreCase(sortKey)) {
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

    private void printAllOpportunitiesTable(List<InternshipOpportunity> list) {
        System.out.printf(
                "%-4s %-15s %-25s %-20s %-20s %-20s %-20s %-12s %-16s %-16s%n",
                "S/N", "Opportunity ID", "Internship Title", "Preferred Major",
                "Internship Level", "Number of Slots", "Company",
                "Open Date", "Closing Date", "Status"
        );
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

        if (list.isEmpty()) {
            System.out.println("✗ No internship opportunities match your current filters.\n");
            return;
        }

        int i = 1;
        for (InternshipOpportunity o : list) {
            String slotsStr = String.format("%d/%d", o.getConfirmedSlots(), o.getSlots());
            System.out.printf(
                    "%-4d %-15s %-25s %-20s %-20s %-20s %-20s %-12s %-16s %-16s%n",
                    i++,
                    o.getId(),
                    o.getTitle(),
                    String.valueOf(o.getPreferredMajor()),
                    String.valueOf(o.getLevel()),
                    slotsStr,
                    o.getCompanyName(),
                    String.valueOf(o.getOpenDate()),
                    String.valueOf(o.getCloseDate()),
                    o.getStatus()
            );
        }

        System.out.println("\n(Total: " + list.size() + " internship opportunities)\n");
    }

    /** view all withdrawal requests (read-only) */
    private void viewAllWithdrawalRequests() {
        ConsoleUI.sectionHeader("Career Center Staff > View All Withdrawal Requests");

        // get all (pending + approved + rejected, depending on implementation)
        List<WithdrawalRequest> all = applicationService.getAllWithdrawalRequests();

        if (all == null || all.isEmpty()) {
            System.out.println("✓ No withdrawal requests found.\n");
            System.out.print("Press enter to return... ");
            sc.nextLine();
            return;
        }

        // table header
        System.out.printf("%-4s %-10s %-15s %-15s %-15s %-20s %-15s %-15s %-20s%n",
                "S/N", "Request ID", "Application ID", "Student ID", "Opportunity ID", "Internship Title", "Company", "Status", "Requested At");
        System.out.println("----------------------------------------------------------------------------------------------------------------------------------------");

        int i = 1;
        for (WithdrawalRequest req : all) {
            System.out.printf("%-4d %-10s %-15s %-15s %-15s %-20s %-15s %-15s %-20s%n",
                    i++,
                    req.getId(),
                    req.getApplication().getId(),
                    req.getRequestedBy().getId(),
                    req.getApplication().getOpportunity().getId(),
                    req.getApplication().getOpportunity().getTitle(),
                    req.getApplication().getOpportunity().getCompanyName(),
                    req.getStatus(),
                    req.getRequestedAt());
        }

        System.out.println("\n(Total: " + all.size() + " withdrawal requests)\n");
        System.out.println();
        System.out.print("Press enter to return... ");
        sc.nextLine();
        ConsoleUI.sectionHeader("Career Center Staff View");
    }

    private void generateReports() {
        ConsoleUI.sectionHeader("Career Center Staff View > Generate Internship Opportunities Report");

        List<InternshipOpportunity> all = oppService.getAllOpportunities();

        if (all.isEmpty()) {
            System.out.println("✗ No internship opportunities found.\n");
            System.out.print("Press enter to return... ");
            sc.nextLine();
            ConsoleUI.sectionHeader("Career Center Staff View");
            return;
        }

        final OpportunityStatus[] statusFilter = new OpportunityStatus[1];
        final Major[] majorFilter = new Major[1];
        final InternshipLevel[] levelFilter = new InternshipLevel[1];

        final String[] sortField = new String[1];      // "id", "title", "company", "level", "major", "status"
        final boolean[] sortDescending = new boolean[1];
        sortDescending[0] = false; // default ascending

        // ───────── filter menu ─────────
        System.out.println("───────────────────────────────────────────────");
        System.out.println("   Internship Opportunity Report Filtering");
        System.out.println("───────────────────────────────────────────────");

        // status filter
        while (true) {
            System.out.println("\nFilter by Opportunity Status:");
            System.out.println("[1] Pending");
            System.out.println("[2] Approved");
            System.out.println("[3] Filled");
            System.out.println("[4] Rejected");
            System.out.println("[Enter] No Filter");
            System.out.println();
            System.out.print("Enter choice: ");

            String s = sc.nextLine().trim().toLowerCase();

            if (s.isEmpty()) {
                statusFilter[0] = null;
                break;
            }

            switch (s) {
                case "1" -> statusFilter[0] = OpportunityStatus.PENDING;
                case "2" -> statusFilter[0] = OpportunityStatus.APPROVED;
                case "3" -> statusFilter[0] = OpportunityStatus.FILLED;
                case "4" -> statusFilter[0] = OpportunityStatus.REJECTED;
                default -> {
                    System.out.println("✗ Invalid option. Please try again.\n");
                    continue;
                }
            }
            break;
        }

        // major filter
        while (true) {
            System.out.println("\nFilter by Preferred Major:");
            System.out.println("[1] CSC");
            System.out.println("[2] DSAI");
            System.out.println("[3] CEG");
            System.out.println("[4] IEM");
            System.out.println("[5] BCG");
            System.out.println("[6] BCE");
            System.out.println("[Enter] No Filter");
            System.out.println();
            System.out.print("Enter choice: ");

            String s = sc.nextLine().trim().toLowerCase();

            if (s.isEmpty()) {
                majorFilter[0] = null;
                break;
            }

            switch (s) {
                case "1" -> majorFilter[0] = Major.CSC;
                case "2" -> majorFilter[0] = Major.DSAI;
                case "3" -> majorFilter[0] = Major.CEG;
                case "4" -> majorFilter[0] = Major.IEM;
                case "5" -> majorFilter[0] = Major.BCG;
                case "6" -> majorFilter[0] = Major.BCE;
                default -> {
                    System.out.println("✗ Invalid option. Please try again.\n");
                    continue;
                }
            }
            break;
        }

        // level filter
        while (true) {
            System.out.println("\nFilter by Intership Level:");
            System.out.println("[1] Basic");
            System.out.println("[2] Intermediate");
            System.out.println("[3] Advanced");
            System.out.println("[Enter] No Filter");
            System.out.println();
            System.out.print("Enter choice: ");

            String s = sc.nextLine().trim().toLowerCase();

            if (s.isEmpty()) {
                levelFilter[0] = null;
                break;
            }

            switch (s) {
                case "1" -> levelFilter[0] = InternshipLevel.BASIC;
                case "2" -> levelFilter[0] = InternshipLevel.INTERMEDIATE;
                case "3" -> levelFilter[0] = InternshipLevel.ADVANCED;
                default -> {
                    System.out.println("✗ Invalid option. Please try again.\n");
                    continue;
                }
            }
            break;
        }

        // ───────── sort menu ─────────
        System.out.println();
        System.out.println("───────────────────────────────────────────────");
        System.out.println("    Internship Opportunity Report Sorting");
        System.out.println("───────────────────────────────────────────────");

        while (true) {
            System.out.println("\nSort by:");
            System.out.println("[1] Opportunity ID");
            System.out.println("[2] Internship Title");
            System.out.println("[3] Company");
            System.out.println("[4] Internship Level");
            System.out.println("[5] Preferred Major");
            System.out.println("[6] Opportunity Status");
            System.out.println("[7] Number of Slots");
            System.out.println("[Enter] Default (Internship Title)");
            System.out.println();
            System.out.print("Enter choice: ");

            String s = sc.nextLine().trim().toLowerCase();

            // default sort = internship titles
            if (s.isEmpty()) {
                sortField[0] = "title";
                sortDescending[0] = false; 
                break;
            }

            switch (s) {
                case "1" -> sortField[0] = "id";
                case "2" -> sortField[0] = "title";
                case "3" -> sortField[0] = "company";
                case "4" -> sortField[0] = "level";
                case "5" -> sortField[0] = "major";
                case "6" -> sortField[0] = "status";
                case "7" -> sortField[0] = "slots"; 
                default -> {
                    System.out.println("✗ Invalid option. Please try again.\n");
                    continue;
                }
            }
            break;
        }

        if (sortField[0] != null) {
            while (true) {
                System.out.println("\nSort Order:");
                System.out.println("[1] Ascending");
                System.out.println("[2] Descending");
                System.out.println("[Enter] Default (Ascending)");
                System.out.println();
                System.out.print("Enter choice: ");

                String s = sc.nextLine().trim().toLowerCase();

                if (s.isEmpty() || s.equals("1")) {
                    sortDescending[0] = false;
                    break;
                }
                if (s.equals("2")) {
                    sortDescending[0] = true;
                    break;
                }

                System.out.println("✗ Invalid option. Please try again.\n");
            }
        }

        // ───────── build filtercriteria for report metadata ─────────
        FilterCriteria criteria = new FilterCriteria();
        if (statusFilter[0] != null) {
            criteria.setStatus(statusFilter[0]);
        }
        if (majorFilter[0] != null) {
            criteria.setPreferredMajor(majorFilter[0].name());
        }
        if (levelFilter[0] != null) {
            criteria.setLevel(levelFilter[0]);
        }

        // ───────── filtered results ─────────
        List<InternshipOpportunity> result = all.stream()
                .filter(o -> statusFilter[0] == null || o.getStatus() == statusFilter[0])
                .filter(o -> majorFilter[0] == null || o.getPreferredMajor() == majorFilter[0])
                .filter(o -> levelFilter[0] == null || o.getLevel() == levelFilter[0])
                .collect(Collectors.toList());

        // ───────── apply sorting if requested ─────────
        if (sortField[0] != null) {
            Comparator<InternshipOpportunity> cmp = switch (sortField[0]) {
                case "id"     -> Comparator.comparing(InternshipOpportunity::getId, String.CASE_INSENSITIVE_ORDER);
                case "title"  -> Comparator.comparing(InternshipOpportunity::getTitle, String.CASE_INSENSITIVE_ORDER);
                case "company"-> Comparator.comparing(InternshipOpportunity::getCompanyName, String.CASE_INSENSITIVE_ORDER);
                case "level"  -> Comparator.comparing(InternshipOpportunity::getLevel);
                case "major"  -> Comparator.comparing(o -> o.getPreferredMajor().name(), String.CASE_INSENSITIVE_ORDER);
                case "status" -> Comparator.comparing(o -> o.getStatus().name(), String.CASE_INSENSITIVE_ORDER);
                case "slots"  -> Comparator.comparingInt(InternshipOpportunity::getSlots);
                default       -> null;
            };

            if (cmp != null) {
                if (sortDescending[0]) {
                    cmp = cmp.reversed();
                }
                result.sort(cmp);
            }
        }

        // ───────── handle empty after filter/sort ─────────
        if (result.isEmpty()) {

            // current filters
            StringBuilder current = new StringBuilder("Current Filters: ");
            boolean noFilter = true;

            if (statusFilter[0] != null) {
                current.append("status = ").append(statusFilter[0].name().toLowerCase()).append("  ");
                noFilter = false;
            }
            if (majorFilter[0] != null) {
                current.append("major = ").append(majorFilter[0].name().toLowerCase()).append("  ");
                noFilter = false;
            }
            if (levelFilter[0] != null) {
                current.append("level = ").append(levelFilter[0].name().toLowerCase()).append("  ");
                noFilter = false;
            }
            if (noFilter) {
                current = new StringBuilder("Current Filters: None");
            }

            // current sort
            // current sort (friendly label)
            StringBuilder sortInfo = new StringBuilder("Current Sorting: ");
            if (sortField[0] == null) {
                sortInfo.append("None");
            } else {
                String sortLabel = switch (sortField[0]) {
                    case "id"     -> "Opportunity ID";
                    case "title"  -> "Internship Title";
                    case "company"-> "Company Name";
                    case "level"  -> "Internship Level";
                    case "major"  -> "Preferred Major";
                    case "status" -> "Opportunity Status";
                    case "slots"  -> "Number of Slots";
                    default       -> sortField[0];
                };

                sortInfo.append(sortLabel)
                        .append(" (")
                        .append(sortDescending[0] ? "descending" : "ascending")
                        .append(")");
            }

            System.out.println();
            System.out.println(current);   // active filters string (you already built this)
            System.out.println(sortInfo);  // new friendly "current sorting"
            System.out.println("✗ No internship opportunities matched your filters.\n");

            System.out.print("Press enter to return... ");
            sc.nextLine();
            ConsoleUI.sectionHeader("Career Center Staff View");
            return;
        }

        // ───────── create report entity ─────────
        Report report = new Report(staff, criteria, result);
        System.out.println("\n" + report.getSummary() + "\n");

        // ───────── show current filters & sorting (non-empty) ─────────
        StringBuilder current2 = new StringBuilder("Current Filters: ");
        boolean noFilter2 = true;

        if (statusFilter[0] != null) {
            current2.append("Opportunity Status = ").append(statusFilter[0].name().toLowerCase()).append("  ");
            noFilter2 = false;
        }
        if (majorFilter[0] != null) {
            current2.append("Preferred Major = ").append(majorFilter[0].name().toLowerCase()).append("  ");
            noFilter2 = false;
        }
        if (levelFilter[0] != null) {
            current2.append("Internship Level = ").append(levelFilter[0].name().toLowerCase()).append("  ");
            noFilter2 = false;
        }
        if (noFilter2) {
            current2 = new StringBuilder("Current Filters: None");
        }

        // friendly sorting label
        StringBuilder sortInfo2 = new StringBuilder("Current Sorting: ");
        if (sortField[0] == null) {
            sortInfo2.append("None");
        } else {
            String sortLabel = switch (sortField[0]) {
                case "id"     -> "Opportunity ID";
                case "title"  -> "Internship Title";
                case "company"-> "Company Name";
                case "level"  -> "Internship Level";
                case "major"  -> "Preferred Major";
                case "status" -> "Opportunity Status";
                case "slots"  -> "Number of Slots";
                default       -> sortField[0];
            };

            sortInfo2.append(sortLabel)
                    .append(" (")
                    .append(sortDescending[0] ? "descending" : "ascending")
                    .append(")");
        }

        System.out.println(current2);
        System.out.println(sortInfo2 + "\n");

        // ───────── print table ─────────
        String header = String.format(
            "%-4s %-15s %-25s %-20s %-20s %-20s %-15s %-20s %-15s %-15s",
            "S/N", "Opportunity ID", "Internship Title", "Company", "Internship level",
            "Preferred Major", "Status", "Number of Slots", "Open Date", "Close Date"
        );
        System.out.println(header);
        String separator = "------------------------------------------------------------------------------------------------------------------------------------------------------------------------------";
        System.out.println(separator);

        int i = 1;
        for (InternshipOpportunity o : result) {
            String slots = o.getConfirmedSlots() + "/" + o.getSlots();
            String row = String.format(
                "%-4d %-15s %-25s %-20s %-20s %-20s %-15s %-20s %-15s %-15s",
                i++,
                o.getId(),
                o.getTitle(),
                o.getCompanyName(),
                o.getLevel(),
                o.getPreferredMajor(),
                o.getStatus(),
                slots,
                o.getOpenDate(),
                o.getCloseDate()
            );
            System.out.println(row);
        }

        System.out.println("\n(Total: " + result.size() + " internship opportunities)\n");

        // ───────── save as csv? ─────────
        while (true) {
            System.out.print("Do you want to save this report as a .csv file? (y/n): ");
            String save = sc.nextLine().trim().toLowerCase();

            if (save.equals("y") || save.equals("yes")) {
                saveReportAsCSV(report, "reports/report_" + System.currentTimeMillis() + ".csv");
                break;
            }
            if (save.equals("n") || save.equals("no")) {
                break;
            }
            System.out.println("✗ Invalid input. Please try again.\n");
        }

        ConsoleUI.sectionHeader("Career Center Staff View");
    }

    public void saveReportAsCSV(Report report, String path) {

        try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {

            // write summary only
            writer.println("summary");
            writer.printf("\"%s\"%n%n", report.getSummary().toLowerCase());

            // write headers
            writer.println("id,title,company,level,major,status,confirmed_slots,total_slots,open_date,close_date");

            // write each opportunity
            for (InternshipOpportunity o : report.getOpportunities()) {
                writer.printf(
                    "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d,%d,%s,%s%n",
                    o.getId(),
                    o.getTitle(),
                    o.getCompanyName(),
                    o.getLevel(),
                    o.getPreferredMajor(),
                    o.getStatus(),
                    o.getConfirmedSlots(),
                    o.getSlots(),
                    o.getOpenDate(),
                    o.getCloseDate()
                );
            }

            System.out.println("✓ report successfully saved to " + path);

        } catch (IOException e) {
            System.out.println("✗ failed to save report: " + e.getMessage());
        }
    }

    private void forceFirstTimePasswordChange() {
        System.out.println("\nYou are currently using the default password.");
        System.out.println("Please change your password before accessing the system.\n");

        while (staff.isFirstLogin()) {

            System.out.print("Enter new password: ");
            String newPwd = sc.nextLine().trim();

            System.out.print("Confirm new password: ");
            String confirm = sc.nextLine().trim();

            if (!newPwd.equals(confirm)) {
                System.out.println("✗ Passwords do not match. please try again.\n");
                continue;
            }

            if (!staff.forceFirstTimePasswordChange(newPwd)) {
                System.out.println("✗ Unable to change password. Please try again.\n");
                continue;
            }

            loader.saveUsers(users);
            System.out.println("\n✓ Password updated. You may now use the system.\n");
        }
    }
}