package entity;

public class CareerCenterStaff extends User {
    private final String staffDepartment;

    public CareerCenterStaff(String id, String name, String staffDepartment) {
        super(id, name);
        this.staffDepartment = staffDepartment;
    }

    /** 
     * @return String
     */
    public String getStaffDepartment() { 
        return staffDepartment; 
    }
}
