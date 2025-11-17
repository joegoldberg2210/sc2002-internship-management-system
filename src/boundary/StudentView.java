package boundary;

import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import control.ApplicationService;
import control.DataLoader;
import control.OpportunityService;
import entity.Application;
import entity.FilterCriteria;
import entity.InternshipOpportunity;
import entity.Student;
import entity.User;
import entity.WithdrawalRequest;
import enumerations.ApplicationStatus;
import enumerations.InternshipLevel;
import enumerations.OpportunityStatus;
import ui.ConsoleUI;

public class StudentView {
    private final Scanner sc;
    private final Student student;
    private final List<User> users;
    private final DataLoader loader;
    private final OpportunityService opportunityService;
    private final ApplicationService applicationService;

    private final FilterCriteria availableFilter = new FilterCriteria();
    private String availableSortKey = "title";       
    private boolean availableSortDescending = false; 

    public StudentView(Scanner sc,
                       Student student,
                       List<User> users,
                       DataLoader loader,
                       OpportunityService opportunityService,
                       ApplicationService applicationService) {
        this.sc = sc;
        this.student = student;
        this.users = users;
        this.loader = loader;
        this.opportunityService = opportunityService;
        this.applicationService = applicationService;
    }

    public void run() {
        while (student.isFirstLogin()) {
            forceFirstTimePasswordChange();
        }

        ConsoleUI.sectionHeader("Student View");

        while (true) {
            showMenu();
            System.out.print("Enter choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    manageAccount();
                    break;
                case "2":
                    viewAvailableInternships();
                    break;
                case "3":
                    applyForAvailableInternships();
                    break;
                case "4":
                    viewApplications();
                    break;
                case "5":
                    viewPendingInternshipOffers();
                    break;
                case "6":
                    viewAcceptedInternship();
                    break;
                case "7":
                    withdrawApplication();
                    break;
                case "8":
                    viewMyWithdrawalRequests();
                    break;
                case "logout":
                    System.out.println("\n✓ You have logged out of your account.\n");
                    return;
                default:
                    System.out.println("✗ Invalid choice, please try again.\n");
            }
        }
    }

    private void showMenu() {
        System.out.println("(1) Manage Account");
        System.out.println("(2) View Available Internships");
        System.out.println("(3) Apply For Internships");
        System.out.println("(4) View My Submitted Applications");
        System.out.println("(5) Accept/Reject Internship Offers");
        System.out.println("(6) View Accepted Internship Placement");
        System.out.println("(7) Withdraw Internship Application");
        System.out.println("(8) View My Withdrawal Requests");
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
            case "1":
                viewProfile();
                break;
            case "2":
                changePassword();
                break;
            case "0":
                ConsoleUI.sectionHeader("Student View");
                break;
            default:
                ConsoleUI.sectionHeader("Student View");
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
                System.out.println("✗ New password cannot be the same as your current password.");
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

        boolean successful = newPwd.equals(confirm) && student.changePassword(current, newPwd);

        if (successful) {
            student.setFirstLogin(false);
            System.out.println("\n✓ Password changed successfully!");
            loader.saveUsers(users);
        } else {
            System.out.println("\n✗ Unable to change password. Please try again.");
        }
        ConsoleUI.sectionHeader("Student View");
    }

    private void viewAvailableInternships() {
        ConsoleUI.sectionHeader("Student View > View Available Internships");

        while (true) {
            List<InternshipOpportunity> available =
                    opportunityService.findBy(student, availableFilter);

            Comparator<InternshipOpportunity> cmp;
            if ("company".equalsIgnoreCase(availableSortKey)) {
                cmp = Comparator.comparing(InternshipOpportunity::getCompanyName,
                        String.CASE_INSENSITIVE_ORDER);
            } else if ("slots".equalsIgnoreCase(availableSortKey)) {
                cmp = Comparator.comparingInt(InternshipOpportunity::getSlots);
            } else if ("openDate".equalsIgnoreCase(availableSortKey)) {
                cmp = Comparator.comparing(InternshipOpportunity::getOpenDate);
            } else if ("closeDate".equalsIgnoreCase(availableSortKey)) {
                cmp = Comparator.comparing(InternshipOpportunity::getCloseDate);
            } else {
                cmp = Comparator.comparing(InternshipOpportunity::getTitle,
                        String.CASE_INSENSITIVE_ORDER);
                availableSortKey = "title";
            }

            if (availableSortDescending) {
                cmp = cmp.reversed();
            }
            available.sort(cmp);

            printCurrentFilterAndSort(availableFilter, availableSortKey, availableSortDescending);
            printAvailableInternshipsTable(available);

            System.out.println("(1) Edit Filter");
            System.out.println("(2) Edit Sort");
            System.out.println("(0) Reset Filter & Sort");
            System.out.print("Enter choice (blank to cancel): ");
            String choice = sc.nextLine().trim();

            if (choice.isEmpty()) {
                ConsoleUI.sectionHeader("Student View");
                return;
            } else if ("1".equals(choice)) {
                while (true) {
                    System.out.println();
                    System.out.println("Filter internships by:");
                    System.out.println("(1) Internship Level");
                    System.out.println("(2) Company Name");
                    System.out.print("Enter choice (blank to cancel): ");
                    String f = sc.nextLine().trim();

                    if (f.isEmpty()) {
                        break;
                    } else if ("1".equals(f)) {
                        System.out.println();
                        System.out.println("Select Internship Level:");
                        System.out.println("(1) Basic");
                        System.out.println("(2) Intermediate");
                        System.out.println("(3) Advanced");
                        System.out.print("Enter choice (blank to cancel): ");
                        String lv = sc.nextLine().trim();

                        if (lv.isEmpty()) {
                            System.out.println("Internship level filter unchanged.\n");
                        } else if ("1".equals(lv)) {
                            availableFilter.setLevel(InternshipLevel.BASIC);
                            System.out.println("✓ Internship level filter set to BASIC.\n");
                        } else if ("2".equals(lv)) {
                            availableFilter.setLevel(InternshipLevel.INTERMEDIATE);
                            System.out.println("✓ Internship level filter set to INTERMEDIATE.\n");
                        } else if ("3".equals(lv)) {
                            availableFilter.setLevel(InternshipLevel.ADVANCED);
                            System.out.println("✓ Internship level filter set to ADVANCED.\n");
                        } else {
                            System.out.println("✗ Invalid choice.\n");
                        }
                        break;

                    } else if ("2".equals(f)) {
                        System.out.println();
                        System.out.print("Enter company keyword (blank to keep current company filter): ");
                        String kw = sc.nextLine().trim();
                        if (kw.isEmpty()) {
                            System.out.println("Company filter unchanged.\n");
                        } else {
                            availableFilter.setCompany(kw);
                            System.out.println("✓ Company filter updated.\n");
                        }
                        break;

                    } else {
                        System.out.println("✗ Invalid choice.\n");
                    }
                }

            } else if ("2".equals(choice)) {
                System.out.println();
                System.out.println("Sort by:");
                System.out.println("(1) Internship Title");
                System.out.println("(2) Company");
                System.out.println("(3) Number of Slots");
                System.out.println("(4) Open Date");
                System.out.println("(5) Close Date");
                System.out.print("Enter choice (blank to cancel): ");
                String s = sc.nextLine().trim();

                if (!s.isEmpty()) {
                    if ("1".equals(s)) {
                        availableSortKey = "title";
                    } else if ("2".equals(s)) {
                        availableSortKey = "company";
                    } else if ("3".equals(s)) {
                        availableSortKey = "slots";
                    } else if ("4".equals(s)) {
                        availableSortKey = "openDate";
                    } else if ("5".equals(s)) {
                        availableSortKey = "closeDate";
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
                            availableSortDescending = false;
                        } else if ("2".equals(order)) {
                            availableSortDescending = true;
                        } else {
                            System.out.println("✗ Invalid choice. Keeping previous order.\n");
                        }
                    }
                }

            } else if ("0".equals(choice)) {
                availableFilter.setStatus(null);
                availableFilter.setPreferredMajor(null);
                availableFilter.setLevel(null);
                availableFilter.setCompany(null);
                availableFilter.setClosingDateBefore(null);

                availableSortKey = "title";
                availableSortDescending = false;

                System.out.println("\n✓ Filters and sorting reset to default.\n");
            } else {
                System.out.println("✗ Invalid choice. Please try again.\n");
            }
        }
    }

    /** 
     * @param filter
     * @param sortKey
     * @param sortDescending
     */
    private void printCurrentFilterAndSort(FilterCriteria filter,
                                           String sortKey,
                                           boolean sortDescending) {
        StringBuilder fb = new StringBuilder("Current Filters: ");
        boolean any = false;

        if (filter.getLevel() != null) {
            fb.append("Internship Level = ")
              .append(filter.getLevel().name().toLowerCase())
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
            fb = new StringBuilder("Current Filters: None");
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

    /** 
     * @param available
     */
    private void printAvailableInternshipsTable(List<InternshipOpportunity> available) {
        System.out.printf(
                "%-4s %-15s %-25s %-20s %-20s %-20s %-20s %-12s %-12s%n",
                "S/N", "Opportunity ID", "Internship Title", "Internship Level",
                "Company", "Preferred Major", "Number of Slots", "Open Date", "Close Date");
        System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------");

        if (available.isEmpty()) {
            System.out.println("✗ No internship opportunities match your current filters.\n");
            return;
        }

        int i = 1;
        for (InternshipOpportunity o : available) {
            String slotsStr = String.format("%d/%d", o.getConfirmedSlots(), o.getSlots());
            System.out.printf(
                    "%-4d %-15s %-25s %-20s %-20s %-20s %-20s %-12s %-12s%n",
                    i++,
                    o.getId(),
                    o.getTitle(),
                    String.valueOf(o.getLevel()),
                    o.getCompanyName(),
                    String.valueOf(o.getPreferredMajor()),
                    slotsStr,
                    o.getOpenDate(),
                    o.getCloseDate()
            );
        }

        System.out.println();
        System.out.println("(Total: " + available.size() + " internship opportunities)\n");
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
                "S/N", "Application ID", "Opportunity ID", "Internship Title",
                "Internship Level", "Company", "Preferred Major", "Status", "Applied Date"
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

        System.out.println("\n(Total number of active applications: " + activeCount + ")\n");
        System.out.print("Press enter to return... ");
        sc.nextLine();
        ConsoleUI.sectionHeader("Student View");
    }

    private void viewPendingInternshipOffers() {
        ConsoleUI.sectionHeader("Student View > Accept/Reject Internship Offers");

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
            System.out.print("Press enter to return... ");
            sc.nextLine();
            ConsoleUI.sectionHeader("Student View");
            return;
        }

        System.out.printf("%-4s %-15s %-15s %-25s %-20s %-20s %-20s %-20s%n",
                "S/N", "Application ID", "Opportunity ID", "Internship Title",
                "Internship Level", "Company", "Preferred Major", "Application Status");
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------");

        int i = 1;
        for (Application a : offers) {
            System.out.printf("%-4d %-15s %-15s %-25s %-20s %-20s %-20s %-20s%n",
                    i++,
                    a.getId(),
                    a.getOpportunity().getId(),
                    a.getOpportunity().getTitle(),
                    a.getOpportunity().getLevel(),
                    a.getOpportunity().getCompanyName(),
                    a.getOpportunity().getPreferredMajor(),
                    a.getStatus());
        }
        System.out.println();

        System.out.print("\nEnter Application ID: ");
        String appId = sc.nextLine().trim();

        Application selected = offers.stream()
                .filter(a -> a.getId().equalsIgnoreCase(appId))
                .findFirst()
                .orElse(null);

        if (selected == null) {
            System.out.println("✗ Invalid Application ID. Please check and try again.");
            System.out.print("Press enter to return... ");
            sc.nextLine();
            ConsoleUI.sectionHeader("Student View");
            return;
        }

        System.out.println("\n────────────────────────────────────────────────────────────");
        System.out.println("                     Application Details                   ");
        System.out.println("────────────────────────────────────────────────────────────");
        System.out.printf("%-18s: %s%n", "Application ID", selected.getId());
        System.out.printf("%-18s: %s%n", "Opportunity ID", selected.getOpportunity().getId());
        System.out.printf("%-18s: %s%n", "Internship Title", selected.getOpportunity().getTitle());
        System.out.printf("%-18s: %s%n", "Internship Level", selected.getOpportunity().getLevel());
        System.out.printf("%-18s: %s%n", "Company", selected.getOpportunity().getCompanyName());
        System.out.printf("%-18s: %s%n", "Preferred Major", selected.getOpportunity().getPreferredMajor());
        System.out.printf("%-18s: %s%n", "Status", selected.getStatus());
        System.out.println("────────────────────────────────────────────────────────────");

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
        List<Application> eligible = myApps.stream()
                .filter(a -> a.getStatus() == ApplicationStatus.PENDING
                        || a.getStatus() == ApplicationStatus.SUCCESSFUL)
                .filter(a -> !applicationService.hasPendingWithdrawal(a))
                .collect(Collectors.toList());

        if (eligible.isEmpty()) {
            System.out.println("✗ You have no applications eligible for withdrawal.\n");
            System.out.print("Press enter to return... ");
            sc.nextLine();
            ConsoleUI.sectionHeader("Student View");
            return;
        }

        System.out.println();
        System.out.printf("%-4s %-15s %-15s %-25s %-22s %-20s%n",
                "S/N","Application ID","Opportunity ID","Internship Title","Company","Application Status");
        System.out.println("------------------------------------------------------------------------------------------------------");
        int i = 1;
        for (Application a : eligible) {
            InternshipOpportunity o = a.getOpportunity();
            System.out.printf("%-4d %-15s %-15s %-25s %-22s %-20s%n",
                    i++,
                    a.getId(),
                    o.getId(),
                    o.getTitle(),
                    o.getCompanyName(),
                    String.valueOf(a.getStatus()));
        }
        System.out.println();

        Application selected = null;

        while (true) {
            System.out.print("Enter Application ID to request withdrawal (blank to cancel): ");
            String appId = sc.nextLine().trim();

            if (appId.isEmpty()) {
                System.out.println("Withdrawal request cancelled.\n");
                ConsoleUI.sectionHeader("Student View");
                return;
            }

            selected = eligible.stream()
                    .filter(a -> a.getId().equalsIgnoreCase(appId))
                    .findFirst()
                    .orElse(null);

            if (selected != null) break;

            System.out.println("✗ Invalid Application ID. Please try again.\n");
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

        applicationService.submitWithdrawalRequest(student, selected);

        System.out.print("Press enter to return... ");
        sc.nextLine();
        ConsoleUI.sectionHeader("Student View");
    }

    private void viewAcceptedInternship() {
        ConsoleUI.sectionHeader("Student View > View Accepted Internship Placement");

        List<Application> myApps = applicationService.getApplicationsForStudent(student);
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

        System.out.println("Below are the details of your accepted internship placement:\n");
        for (Application a : accepted) {
            InternshipOpportunity o = a.getOpportunity();
            System.out.println("\n────────────────────────────────────────────────────────────");
            System.out.println("               Internship Placement Details                 ");
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

    private void viewMyWithdrawalRequests() {
        ConsoleUI.sectionHeader("Student View > View My Withdrawal Requests");

        List<WithdrawalRequest> requests =
                applicationService.getRequestsForStudent(student);

        if (requests.isEmpty()) {
            System.out.println("✗ You have not submitted any withdrawal requests.\n");
            System.out.print("Press enter to return... ");
            sc.nextLine();
            ConsoleUI.sectionHeader("Student View");
            return;
        }

        System.out.printf("%-4s %-10s %-15s %-15s %-25s %-22s %-20s%n",
                "S/N",
                "Request ID",
                "Application ID",
                "Opportunity ID",
                "Internship Title",
                "Company",
                "Status");

        System.out.println("--------------------------------------------------------------------------------------------------------");

        int i = 1;
        for (WithdrawalRequest req : requests) {
            Application app = req.getApplication();
            InternshipOpportunity opp = app.getOpportunity();

            System.out.printf("%-4d %-10s %-15s %-15s %-25s %-22s %-20s%n",
                    i++,
                    req.getId(),
                    app.getId(),
                    opp.getId(),
                    opp.getTitle(),
                    opp.getCompanyName(),
                    req.getStatus());
        }

        System.out.println();
        System.out.println("(Total: " + requests.size() + " withdrawal request(s))\n");

        System.out.print("Press enter to return... ");
        sc.nextLine();
        ConsoleUI.sectionHeader("Student View");
    }

    private void applyForAvailableInternships() {
        ConsoleUI.sectionHeader("Student View > Apply For Internships");

        List<InternshipOpportunity> available = opportunityService.getAllOpportunities().stream()
                .filter(o -> o.getStatus() != OpportunityStatus.REJECTED)
                .filter(o -> o.isEligibleFor(student))
                .collect(Collectors.toList());

        if (available.isEmpty()) {
            System.out.println("✗ No internship opportunities available for you.\n");
            System.out.print("Press enter to return... ");
            sc.nextLine();
            ConsoleUI.sectionHeader("Student View");
            return;
        }

        System.out.println();
        System.out.printf(
                "%-4s %-15s %-25s %-20s %-15s %-15s %-15s %-12s %-12s%n",
                "S/N", "Opportunity ID", "Internship Title", "Internship Level",
                "Company", "Preferred Major", "Number of Slots", "Open Date", "Close Date");
        System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------------------------------");

        int i = 1;
        for (InternshipOpportunity o : available) {
            String slotsStr = String.format("%d/%d", o.getConfirmedSlots(), o.getSlots());

            System.out.printf(
                    "%-4d %-15s %-25s %-20s %-15s %-15s %-15s %-12s %-12s%n",
                    i++,
                    o.getId(),
                    o.getTitle(),
                    o.getLevel(),
                    o.getCompanyName(),
                    o.getPreferredMajor(),
                    slotsStr,
                    o.getOpenDate(),
                    o.getCloseDate()
            );
        }

        System.out.println("\n(Total: " + available.size() + " internship opportunities)\n");

        InternshipOpportunity selected = null;

        while (true) {
            System.out.print("Enter Opportunity ID to apply (blank to cancel): ");
            String id = sc.nextLine().trim();

            if (id.isEmpty()) {
                ConsoleUI.sectionHeader("Student View");
                return;
            }

            selected = available.stream()
                    .filter(o -> o.getId().equalsIgnoreCase(id))
                    .findFirst()
                    .orElse(null);

            if (selected != null) break;

            System.out.println("✗ Invalid opportunity ID. Please try again.\n");
        }

        if (!selected.isOpenFor(student)) {
            System.out.println("✗ This internship is not currently open for applications.");
            System.out.println();
            System.out.print("Press enter to return... ");
            sc.nextLine();
            ConsoleUI.sectionHeader("Student View");
            return;
        }

        List<Application> myApps = applicationService.getApplicationsForStudent(student);
        boolean hasPendingForThisOpp = false;

        for (Application a : myApps) {
            if (a.getOpportunity().getId().equalsIgnoreCase(selected.getId())
                    && a.getStatus() == ApplicationStatus.PENDING) {
                hasPendingForThisOpp = true;
                break;
            }
        }

        if (hasPendingForThisOpp) {
            System.out.println("✗ You already have a pending application for this internship and cannot apply again.\n");
            System.out.println();
            System.out.print("Press enter to return... ");
            sc.nextLine();
            ConsoleUI.sectionHeader("Student View");
            return;
        }

        String confirm;
        boolean proceed = false;

        while (true) {
            System.out.printf("Apply for '%s' at %s? (y/n): ",
                    selected.getTitle(), selected.getCompanyName());
            confirm = sc.nextLine().trim().toLowerCase();

            if (confirm.equals("y") || confirm.equals("yes")) {
                proceed = true;
                break;
            }

            if (confirm.equals("n") || confirm.equals("no")) {
                System.out.println("Application cancelled.\n");
                ConsoleUI.sectionHeader("Student View");
                return;
            }

            System.out.println("✗ Invalid input. Please try again.\n");
        }

        if (proceed) {
            // 5) final guard: block if there is any active app (pending/successful) for this opportunity
            boolean hasActive = applicationService.hasActiveApplication(student, selected);

            if (hasActive) {
                System.out.println("✗ You already have a pending or successful application for this internship and cannot apply again.\n");
            } else {
                Application created = applicationService.applyForOpportunity(student, selected);

                if (created != null) {
                    System.out.println("✓ Application submitted successfully - " + created.getId());
                } else {
                    System.out.println("✗ Unable to submit application. Please try again later.");
                }
            }
        }

        System.out.print("\nPress enter to return... ");
        sc.nextLine();
        ConsoleUI.sectionHeader("Student View");
    }

    private void forceFirstTimePasswordChange() {
        System.out.println("\nYou are currently using the default password.");
        System.out.println("Please change your password before accessing the system.\n");

        while (student.isFirstLogin()) {

            System.out.print("Enter new password: ");
            String newPwd = sc.nextLine().trim();

            System.out.print("Confirm new password: ");
            String confirm = sc.nextLine().trim();

            if (!newPwd.equals(confirm)) {
                System.out.println("✗ Passwords do not match. please try again.\n");
                continue;
            }

            if (!student.forceFirstTimePasswordChange(newPwd)) {
                System.out.println("✗ Unable to change password. Please try again.\n");
                continue;
            }

            loader.saveUsers(users);
            System.out.println("\n✓ Password updated. You may now use the system.\n");
        }
    }
}