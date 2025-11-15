package boundary;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import control.ApplicationService;
import control.DataLoader;
import control.OpportunityService;
import entity.Application;
import entity.InternshipOpportunity;
import entity.Student;
import entity.User;
import entity.WithdrawalRequest;
import enumerations.ApplicationStatus;
import enumerations.InternshipLevel;
import ui.ConsoleUI;

public class StudentView {
    private final Scanner sc;
    private final Student student;
    private final List<User> users;
    private final DataLoader loader;
    private final OpportunityService opportunityService;
    private final ApplicationService applicationService;

    // simple filter/sort state for the listing screen
    private InternshipLevel filtLevel = null;   // null = any
    private String          filtCompany = null; // null/empty = any (substring match)
    // SLOTS, LEVEL, COMPANY, OPEN, CLOSE (default to SLOTS so “most available” first)
    private String          sortKey = "SLOTS";

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

    // ─────────────────────────────────────────────────────────────────────


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
                case "1" -> manageAccount();
                case "2" -> viewAvailableInternships();
                case "3" -> applyForAvailableInternships();
                case "4" -> viewApplications();
                case "5" -> viewPendingInternshipOffers();
                case "6" -> viewAcceptedInternship();
                case "7" -> withdrawApplication();
                case "8" -> viewMyWithdrawalRequests();
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
        System.out.println("(3) Apply For Internships");
        System.out.println("(4) View My Submitted Applications");
        System.out.println("(5) View Pending Internship Offers");
        System.out.println("(6) View Accepted Internship Placement");
        System.out.println("(7) Withdraw Internship Application");
        System.out.println("(8) View My Withdrawal Requests");
        System.out.println();
        System.out.println("→ Type 'logout' here to logout");
        System.out.println();
    }

    // ───────────────────────── account ─────────────────────────

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
            case "0" -> ConsoleUI.sectionHeader("Student View");
            default -> ConsoleUI.sectionHeader("Student View");
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

    // ───────────────────── applications listing ─────────────────────

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

    // ───────────────────── offers (accept / reject) ─────────────────────

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
            System.out.print("Press enter to return... ");
            sc.nextLine();
            ConsoleUI.sectionHeader("Student View");
            return;
        }

        System.out.printf("%-4s %-15s %-15s %-25s %-20s %-20s %-20s %-20s%n",
                "S/N", "Application ID", "Opportunity ID", "Internship Title",
                "Internship Level", "Company", "Preferred Major", "Application Status");
        System.out.println("---------------------------------------------------------------------------------------------------");

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

        System.out.print("\nDo you want to accept/reject any internship opportunities? (y/n): ");
        String response = sc.nextLine().trim().toUpperCase();
        if (!response.equals("Y")) {
            ConsoleUI.sectionHeader("Student View");
            return;
        }

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

    // ───────────────────── withdrawals (request) ─────────────────────

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

    // ───────────────────── accepted placement detail ─────────────────────

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

    // ───────────────────── listing + filter/sort/apply ─────────────────────

    private List<InternshipOpportunity> queryAvailableWithFilters() {
        return opportunityService.getAllOpportunities().stream()
                // only those open for this student
                .filter(o -> o.isOpenFor(student))
                // filter by level if set
                .filter(o -> filtLevel == null || o.getLevel() == filtLevel)
                // filter by company name if set
                .filter(o -> filtCompany == null || filtCompany.isBlank()
                        || o.getCompanyName().toLowerCase().contains(filtCompany.toLowerCase()))
                // apply sorting based on sortKey
                .sorted((o1, o2) -> {
                    switch (sortKey.toLowerCase()) {
                        case "company" -> {
                            return o1.getCompanyName().compareToIgnoreCase(o2.getCompanyName());
                        }
                        case "close" -> {
                            return o1.getCloseDate().compareTo(o2.getCloseDate());
                        }
                        case "level" -> {
                            return o1.getLevel().compareTo(o2.getLevel());
                        }
                        default -> {
                            // default sort by opportunity id
                            return o1.getId().compareToIgnoreCase(o2.getId());
                        }
                    }
                })
                .collect(Collectors.toList());
    }

    private void openFilterMenu() {
        System.out.println();
        System.out.println("filter available internships by:");
        System.out.println("  (1) internship level");
        System.out.println("  (2) company name");
        System.out.println("  (9) clear filters");
        System.out.println("  (enter) cancel");
        System.out.print("choose: ");
        String c = sc.nextLine().trim();
        if (c.isEmpty()) return;

        switch (c) {
            case "1" -> {
                System.out.print("enter level (basic/intermediate/advanced) or blank for any: ");
                String v = sc.nextLine().trim();
                filtLevel = v.isEmpty() ? null : InternshipLevel.valueOf(v.toUpperCase());
            }
            case "2" -> {
                System.out.print("enter company keyword (blank for any): ");
                String v = sc.nextLine().trim();
                filtCompany = v.isEmpty() ? null : v;
            }
            case "9" -> {
                filtLevel = null;
                filtCompany = null;
                System.out.println("✓ filters cleared.");
            }
            default -> System.out.println("✗ invalid choice.");
        }
    }

    private void openSortMenu() {
        System.out.println();
        System.out.println("sort available internships by:");
        System.out.println("  (1) available slots");
        System.out.println("  (2) internship level");
        System.out.println("  (3) company name");
        System.out.println("  (4) open date");
        System.out.println("  (5) close date");
        System.out.println("  (enter) cancel");
        System.out.print("choose: ");
        String c = sc.nextLine().trim();
        if (c.isEmpty()) return;

        switch (c) {
            case "1" -> sortKey = "SLOTS";
            case "2" -> sortKey = "LEVEL";
            case "3" -> sortKey = "COMPANY";
            case "4" -> sortKey = "OPEN";
            case "5" -> sortKey = "CLOSE";
            default  -> System.out.println("✗ invalid choice.");
        }
        System.out.println("✓ sort updated.\n");
    }

    private void viewAvailableInternships() {
        ConsoleUI.sectionHeader("Student View > View Available Internships");

        // quick check: are there any opportunities open for this student at all?
        List<InternshipOpportunity> initialAvailable = opportunityService.getAllOpportunities().stream()
                .filter(o -> o.isOpenFor(student))
                .collect(Collectors.toList());

        if (initialAvailable.isEmpty()) {
            System.out.println("✗ No internship opportunities available for you.\n");
            System.out.print("Press enter to return... ");
            sc.nextLine();
            ConsoleUI.sectionHeader("Student View");
            return;
        }

        // set of opportunity ids the student has ever applied for (any status)
        Set<String> appliedOppIds = applicationService.getApplicationsForStudent(student).stream()
                .map(a -> a.getOpportunity().getId())
                .collect(Collectors.toSet());

        while (true) {
            List<Application> myApps = applicationService.getApplicationsForStudent(student);
            boolean hasAccepted = myApps.stream()
                    .anyMatch(a -> a.getStatus() == ApplicationStatus.SUCCESSFUL && a.isAccepted());

            // use your existing filter/sort logic
            List<InternshipOpportunity> available = queryAvailableWithFilters();

            // header line
            System.out.println("(current filter: level=" + (filtLevel == null ? "any" : filtLevel)
                    + ", company=" + (filtCompany == null || filtCompany.isBlank() ? "any" : ("\"" + filtCompany + "\""))
                    + ", sort=" + sortKey.toLowerCase() + ")");
            System.out.println();

            System.out.printf("%-4s %-15s %-25s %-20s %-15s %-15s %-15s %-12s %-12s%n",
                    "S/N", "Opportunity ID", "Internship Title", "Internship Level",
                    "Company", "Preferred Major", "Available Slots", "Open Date", "Close Date");
            System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------");

            if (available.isEmpty()) {
                System.out.println("✗ no internship opportunities match your current filters.\n");
            } else {
                int i = 1;
                for (InternshipOpportunity o : available) {
                    String slotsStr = String.format("%d/%d", o.getConfirmedSlots(), o.getSlots());

                    System.out.printf("%-4s %-15s %-25s %-20s %-15s %-15s %-15s %-12s %-12s%n",
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
                System.out.println("\n(total: " + available.size() + " internship opportunities)\n");
            }

            // simple menu
            System.out.println("(1) filter available internships");
            System.out.println("(2) sort available internships");
            System.out.println("(enter) back");
            System.out.print("choose: ");
            String cmd = sc.nextLine().trim();

            if (cmd.isEmpty()) {
                ConsoleUI.sectionHeader("Student View");
                return;
            } else if (cmd.equals("1")) {
                openFilterMenu();
            } else if (cmd.equals("2")) {
                openSortMenu();
            }
        }
    }

    private void viewMyWithdrawalRequests() {
        ConsoleUI.sectionHeader("Student View > View My Withdrawal Requests");

        // fetch all requests belonging to this student
        List<WithdrawalRequest> requests =
                applicationService.getRequestsForStudent(student);

        if (requests.isEmpty()) {
            System.out.println("✗ You have not submitted any withdrawal requests.\n");
            System.out.print("Press enter to return... ");
            sc.nextLine();
            ConsoleUI.sectionHeader("Student View");
            return;
        }

        // table header
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

        // list all opportunities open for this student
        List<InternshipOpportunity> available = opportunityService.getAllOpportunities().stream()
                .filter(o -> o.isOpenFor(student))
                .collect(Collectors.toList());

        if (available.isEmpty()) {
            System.out.println("✗ No internship opportunities available for you.\n");
            System.out.print("Press enter to return... ");
            sc.nextLine();
            ConsoleUI.sectionHeader("Student View");
            return;
        }

        // print table
        System.out.println();
        System.out.printf(
                "%-4s %-15s %-25s %-20s %-15s %-15s %-15s %-12s %-12s%n",
                "S/N", "Opportunity ID", "Internship Title", "Internship Level",
                "Company", "Preferred Major", "Available Slots", "Open Date", "Close Date");
        System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------");

        int i = 1;
        for (InternshipOpportunity o : available) {
            String slotsStr = String.format("%d/%d", o.getConfirmedSlots(), o.getSlots());

            System.out.printf(
                    "%-4d %-15s %-25s %-20s %-15s %-15s %-15s %-12s %-12s%n",
                    i++,
                    o.getId(),              // opportunity id
                    o.getTitle(),           // internship title
                    o.getLevel(),           // internship level
                    o.getCompanyName(),     // company
                    o.getPreferredMajor(),  // preferred major
                    slotsStr,               // available slots
                    o.getOpenDate(),        // open date
                    o.getCloseDate()        // close date
            );
        }

        System.out.println("\n(Total: " + available.size() + " internship opportunities)\n");

        InternshipOpportunity selected = null;

        // select by opportunity id
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

        if (applicationService.hasAnyApplicationForOpportunity(student, selected)) {
            System.out.println("✗ You have already applied for this internship before and cannot apply again.\n");
            System.out.print("Press enter to return... ");
            sc.nextLine();
            ConsoleUI.sectionHeader("Student View");
            return;
        }

        // confirm apply
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

        // call service
        Application created = applicationService.applyForOpportunity(student, selected);

        if (created != null) {
            System.out.println("✓ Application submitted successfully - " + created.getId());
        } else {
            System.out.println("✗ Unable to submit application.");
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