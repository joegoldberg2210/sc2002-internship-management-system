package control;

import java.util.List;
import java.util.Objects;

import entity.*;

public class AuthControl {

    private final List<User> userList; // list of all registered users
    private User currentUser;          // user currently logged in

    public AuthControl(List<User> userList) {
        this.userList = Objects.requireNonNull(userList, "userList must not be null");
    }

    /** attempt login by canonical id + password, restricted by role */
    public User login(String id, String pwd, int role) {
        String target = User.canonical(id);

        for (User u : userList) {
            if (!matchesRole(u, role)) continue;

            boolean sameId = User.canonical(u.getId()).equals(target);
            if (sameId && u.verifyPassword(pwd)) {

                // extra status check for company representatives
                if (u instanceof CompanyRepresentative rep) {
                    switch (rep.getStatus()) {
                        case PENDING -> {
                            System.out.println("\n✗ Your account is still pending approval. Please wait for staff verification.\n");
                            return null;
                        }
                        case REJECTED -> {
                            System.out.println("\n✗ Your registration was rejected. Please contact the Career Center for clarification.\n");
                            return null;
                        }
                        default -> {
                            // APPROVED → allow login
                        }
                    }
                }

                currentUser = u;
                return u;
            }
        }
        return null;
    }

    private boolean matchesRole(User u, int role) {
        return switch (role) {
            case 1 -> u instanceof Student;
            case 2 -> u instanceof CompanyRepresentative;
            case 3 -> u instanceof CareerCenterStaff;
            default -> false;
        };
    }

    public void logout(User user) {
        if (currentUser != null && currentUser.equals(user)) currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    /** add user only if no existing user (of any type) has the same canonical id */
    public boolean addUser(User u) {
        if (userIdTaken(u.getId())) return false;
        userList.add(u);
        return true;
    }

    /** true if any user (any type) already uses this id (case/space insensitive) */
    public boolean userIdTaken(String id) {
        String target = User.canonical(id);
        for (User u : userList) {
            if (User.canonical(u.getId()).equals(target)) return true;
        }
        return false;
    }

    /** find a user by id (case/space insensitive), or null if not found */
    public User findById(String id) {
        String target = User.canonical(id);
        for (User u : userList) {
            if (User.canonical(u.getId()).equals(target)) return u;
        }
        return null;
    }
}