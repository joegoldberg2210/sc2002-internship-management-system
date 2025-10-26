package control;

import java.util.List;

import entity.*;

public class AuthControl {

	private List<User> userList; // list of all registered users
    private User currentUser;    // user currently logged in

    public AuthControl(List<User> userList) {
        this.userList = userList;
    }

	public User login(String id, String pwd, int role) {
        for (User u : userList) {
            if (!matchesRole(u, role)) continue;
            if (u.getId().equalsIgnoreCase(id) && u.verifyPassword(pwd)) {
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
}