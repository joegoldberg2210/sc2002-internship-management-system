package boundary;

import java.util.Scanner;
import control.AuthControl;
import entity.User;
import ui.ConsoleUI;

public class LoginView {
    private final Scanner sc;

    public LoginView(Scanner sc) {
        this.sc = sc;
    }

    private int selectRole() {
        System.out.println("Choose your role to log in:");
        System.out.println("(1) Student");
        System.out.println("(2) Company Representative");
        System.out.println("(3) Career Center Staff");
        System.out.println();
        System.out.print("Enter choice: ");

        while (true) {
            String input = sc.nextLine().trim();
            if (input.matches("[1-3]")) {
                return Integer.parseInt(input);
            }
            System.out.println();
            System.out.print("✗ Invalid choice. Please enter 1-3: ");
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
        return new String[]{id, pwd};
    }

    public User run(AuthControl auth) {
        ConsoleUI.bigBanner();

        while (true) {
            int role = selectRole();
            String roleName = roleName(role);

            String[] creds = askCredentials(roleName);
            if (creds[0].isEmpty()) continue; // empty id → re-prompt

            User logged = auth.login(creds[0], creds[1], role);
            if (logged != null) {
                System.out.println("✓ Login successful!");
                return logged;
            }

            System.out.println("✗ Invalid username or password. Please try again.\n");
        }
    }
}