package entity;

/** Career center staff with department and preferences. */
public class CareerCenterStaff extends User {
    private final String staffDepartment;
    private UserPreferences preferences; // 0..1

    public CareerCenterStaff(String id, String name, String password, String staffDepartment) {
        super(id, name, password);
        this.staffDepartment = staffDepartment;
    }

    public String getStaffDepartment()            { return staffDepartment; }
    public UserPreferences getPreferences()       { return preferences; }
    public void setPreferences(UserPreferences p) { this.preferences = p; }
}
