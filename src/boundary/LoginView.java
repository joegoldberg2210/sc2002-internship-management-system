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

    // constructor
    public LoginView(Scanner sc) {
        this.sc = sc;
    }

    /**
     * main login interface shown to all users
     * allows user to:
     * (1) login into their existing account
     * (2) register for a new account
     * (#) exit system
     */

    public User run(AuthControl auth, AccountApprovalService approval) {
        ConsoleUI.bigBanner();

        while (true) {
            String menu = mainMenu();
            if (menu.equals("#")) {
                return null; 
            }

            // login flow
            if (menu.equals("1")) {
                int role = selectRole();
                String roleName = roleName(role);

                String[] creds = askCredentials(roleName);
                if (creds[0].isEmpty()) continue;

                // authenticate user credentials
                User logged = auth.login(creds[0], creds[1], role);
                if (logged != null) {
                    System.out.println(" ✓ " + roleName + " Login Successful!");
                    return logged;
                }
                System.out.println("✗ Invalid ID or password. Please try again.\n");
            } 
            // create account flow
            else if (menu.equals("2")) {
                int role = selectRole();
                if (role != 2) {
                    System.out.println("\n✗ Only Company Representatives can register for an account.\n");
                    continue;
                }
                registerCompanyRep(approval, auth);
            }
        }
    }

    // print main menu
    private String mainMenu() {
        System.out.println("What would you like to do?");
        System.out.println("(1) Login");
        System.out.println("(2) Create New Account");
        System.out.println("→ Type '#' here to exit program");
        System.out.println();
        System.out.print("Enter choice: ");

        while (true) {
            String input = sc.nextLine().trim();
            if (input.equals("#") || input.matches("[1-2]")) {
                return input;
            }
            System.out.println("Invalid choice.");
            System.out.print("Enter choice: ");
        }
    }

    private int selectRole() {
        System.out.println();
        System.out.println("What is your role?");
        System.out.println("(1) Student");
        System.out.println("(2) Company Representative");
        System.out.println("(3) Career Center Staff");
        System.out.println();
        System.out.print("Enter choice: ");

        while (true) {
            String input = sc.nextLine().trim();
            if (input.matches("[1-3]")) return Integer.parseInt(input);
            System.out.println("Invalid choice.");
            System.out.print("Enter choice: ");
        }
    }

    private String roleName(int role) {
        return switch (role) {
            case 1 -> "Student";
            case 2 -> "Company Representative";
            case 3 -> "Career Center Staff";
            default -> throw new IllegalArgumentException("Invalid role selected: " + role);
        };
    }

    private String[] askCredentials(String role) {
        ConsoleUI.loginBox(role + " Login");

        String id;
        while (true) {
            System.out.print(role + " ID: ");
            id = sc.nextLine().trim();

            if (id.isEmpty()) {
                System.out.println("✗ " + role + " ID cannot be empty.");
                continue;
            }
            break;
        }

        String pwd;
        while (true) {
            System.out.print("Password: ");
            pwd = sc.nextLine().trim();

            if (pwd.isEmpty()) {
                System.out.println("✗ Password cannot be empty.");
                continue;
            }
            break;
        }

        System.out.println();
        return new String[]{ id, pwd };
    }

    private void registerCompanyRep(AccountApprovalService approval, AuthControl auth) {
        ConsoleUI.sectionHeader("Company Representative Registration");

        String id = promptUniqueCompanyRepId(auth);

        System.out.print("Full Name: ");
        String name = sc.nextLine().trim();
        
        System.out.print("Company Name: ");
        String company = sc.nextLine().trim();

        System.out.print("Department: ");
        String department = sc.nextLine().trim();

        System.out.print("Position: ");
        String position = sc.nextLine().trim();

        CompanyRepresentative rep = new CompanyRepresentative(id, name, company, department, position, AccountStatus.PENDING);

        boolean validID = approval.submitCompanyRepRegistration(rep);
        if (!validID) {
            System.out.println("\n✗ This ID is already registered with us. Please choose another ID.\n");
            return;
        }

        System.out.println("\n✓ Registration submitted. Status: PENDING.");
        System.out.println("You can log in after a Career Center Staff approves your account.\n");
    }

    private String promptUniqueCompanyRepId(AuthControl auth) {
        while (true) {
            System.out.print("Company Representative ID: ");
            String id = sc.nextLine().trim();

            // check if email address is valid
            if (!id.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                System.out.println("✗ Please enter a valid email address.");
                continue;
            }

            // check if the ID already exists in existing company representative records
            if (auth.userIdTaken(id)) {
                System.out.println("✗ This ID is already registered with us. Please choose another ID.\n");
                continue;
            }
            return id;
        }
    }
}