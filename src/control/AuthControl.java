package control;

import java.util.List;
import java.util.Objects;

import entity.*;

public class AuthControl {

    private final List<User> userList; 
    private User currentUser;          

    public AuthControl(List<User> userList) {
        this.userList = Objects.requireNonNull(userList, "userList must not be null");
    }

    /** 
     * @param id
     * @param pwd
     * @param role
     * @return User
     */
    public User login(String id, String pwd, int role) {
        String target = User.canonical(id);

        for (User u : userList) {
            if (!matchesRole(u, role)) continue;

            boolean sameId = User.canonical(u.getId()).equals(target);
            if (sameId && u.verifyPassword(pwd)) {

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
                        }
                    }
                }

                currentUser = u;
                return u;
            }
        }
        return null;
    }

    /** 
     * @param u
     * @param role
     * @return boolean
     */
    private boolean matchesRole(User u, int role) {
        return switch (role) {
            case 1 -> u instanceof Student;
            case 2 -> u instanceof CompanyRepresentative;
            case 3 -> u instanceof CareerCenterStaff;
            default -> false;
        };
    }

    /** 
     * @param user
     */
    public void logout(User user) {
        if (currentUser != null && currentUser.equals(user)) currentUser = null;
    }

    /** 
     * @return User
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /** 
     * @param u
     * @return boolean
     */
    public boolean addUser(User u) {
        if (userIdTaken(u.getId())) return false;
        userList.add(u);
        return true;
    }

    /** 
     * @param id
     * @return boolean
     */
    public boolean userIdTaken(String id) {
        String target = User.canonical(id);
        for (User u : userList) {
            if (User.canonical(u.getId()).equals(target)) return true;
        }
        return false;
    }

    /** 
     * @param id
     * @return User
     */
    public User findById(String id) {
        String target = User.canonical(id);
        for (User u : userList) {
            if (User.canonical(u.getId()).equals(target)) return u;
        }
        return null;
    }
}