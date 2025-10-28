package boundary;

import java.util.Scanner;

import control.AuthControl;
import control.AccountApprovalService;
import entity.CompanyRepresentative;
import entity.User;
import enumerations.AccountStatus;
import ui.ConsoleUI;

public class LoginView {
    private final Scanner sc;

    public LoginView(Scanner sc) {
        this.sc = sc;
    }

    public User run(AuthControl auth, AccountApprovalService approval) {
        ConsoleUI.bigBanner();

        while (true) {
            int role = selectRole();
            String roleName = roleName(role);

            if (role == 2 && wantsRegistration()) {
                registerCompanyRep(approval, auth);
                continue;
            }

            String[] creds = askCredentials(roleName);
            if (creds[0].isEmpty()) continue;

            User logged = auth.login(creds[0], creds[1], role);
            if (logged != null) {
                System.out.println("✓ Login Successful!");
                return logged;
            }

            System.out.println("✗ Invalid ID or password. Please try again.\n");
        }
    }

    private int selectRole() {
        System.out.println("Choose your role:");
        System.out.println("(1) Student");
        System.out.println("(2) Company Representative");
        System.out.println("(3) Career Center Staff");
        System.out.println();
        System.out.print("Enter choice: ");

        while (true) {
            String input = sc.nextLine().trim();
            if (input.matches("[1-3]")) return Integer.parseInt(input);
            System.out.print("Invalid choice. Enter 1-3: ");
        }
    }

    private String roleName(int role) {
        return switch (role) {
            case 1 -> "Student";
            case 2 -> "Company Representative";
            case 3 -> "Career Center Staff";
            default -> "User";
        };
    }

    private String[] askCredentials(String role) {
        ConsoleUI.loginBox(role + " Login");
        System.out.print(role + " ID: ");
        String id = sc.nextLine().trim();
        System.out.print("Password: ");
        String pwd = sc.nextLine().trim();
        System.out.println();
        return new String[]{ id, pwd };
    }

    // ask if the user already has an account; if not, proceed to registration
    private boolean wantsRegistration() {
        System.out.println();
        System.out.print("Do you have an existing account? (y/n): ");
        
        while (true) {
            String ans = sc.nextLine().trim().toLowerCase();

            if (ans.equals("y") || ans.equals("yes")) {
                // user already has an account → go back to login
                return false;
            } 
            else if (ans.equals("n") || ans.equals("no")) {
                // user does not have an account → proceed to registration
                return true;
            } 
            else {
                System.out.print("✗ Please enter 'y' or 'n': ");
            }
        }
    }

    private void registerCompanyRep(AccountApprovalService approval, AuthControl auth) {
        ConsoleUI.sectionHeader("Company Representative Registration");

        System.out.print("Company Representative ID: ");
        String id = sc.nextLine().trim();

        System.out.print("Full Name: ");
        String name = sc.nextLine().trim();

        System.out.print("Company Name: ");
        String company = sc.nextLine().trim();

        System.out.print("Department: ");
        String dept = sc.nextLine().trim();

        System.out.print("Position: ");
        String pos = sc.nextLine().trim();

        // default password policy for first login
        String defaultPwd = "password";

        CompanyRepresentative rep =
            new CompanyRepresentative(id, name, company, dept, pos, AccountStatus.PENDING);

        // submit for approval (status -> pending)
        approval.submitCompanyRepRegistration(rep);

        // make account visible to the system so staff can find/approve it
        try {
            auth.addUser(rep);
        } catch (NoSuchMethodError | UnsupportedOperationException e) {
        }

        System.out.println("\n✓ Registration submitted. Status: PENDING.");
        System.out.println("  You can log in after a career center staff approves your account.\n");
    }
}