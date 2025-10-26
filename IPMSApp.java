import java.util.List;
import java.util.Scanner;

import boundary.LoginView;
import boundary.StudentView;
import control.AuthControl;
import control.DataLoader;
import entity.Student;
import entity.User;

public class IPMSApp {
    public static void main(String[] args) {
        // create the data loader
        DataLoader loader = new DataLoader();

        // load all users from the csv files inside data folder
        List<User> users = loader.loadUsers();

        // check if loading was successful
        if (users.isEmpty()) {
            System.out.println("No users loaded. Please check CSV files.");
        } else {
            System.out.println(users.size() + " Users loaded successfully.\n");

            // print the details of each user (optional)
            for (User u : users) {
                System.out.println("ID: " + u.getId() + " | Name: " + u.getName() + " | User Type: " + u.getClass().getSimpleName());
            }
        }

        try (Scanner sc = new Scanner(System.in)) {

            AuthControl auth = new AuthControl(users);
            LoginView login = new LoginView(sc);

            User logged = login.run(auth); // blocks until successful login

            // post-login routing by user type
            if (logged instanceof Student s) {
                new StudentView(sc, s, users, loader).run();
            }
            // else if (logged instanceof CompanyRepresentative cr) {
            // }
            // else if (logged instanceof CareerCenterStaff staff) {
            // }

            auth.logout(logged);

            // persist any changes
            loader.saveUsers(users);
        }
    }
}