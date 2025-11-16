package entity;

/** Career center staff with department and preferences. */
public class CareerCenterStaff extends User {
    private final String staffDepartment;

    public CareerCenterStaff(String id, String name, String staffDepartment) {
        super(id, name);
        this.staffDepartment = staffDepartment;
    }

    public String getStaffDepartment()            { return staffDepartment; }
}
