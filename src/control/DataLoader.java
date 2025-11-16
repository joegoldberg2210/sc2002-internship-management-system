package control;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import entity.*;
import enumerations.AccountStatus;
import enumerations.Major;

public class DataLoader {
    private static final String DATA_FOLDER       = "data";
    private static final String SERIALIZED_FOLDER = "serialized";

    private static final String USERS_FILE         = SERIALIZED_FOLDER + "/users.ser";
    private static final String OPPORTUNITIES_FILE = SERIALIZED_FOLDER + "/opportunities.ser";
    private static final String APPLICATIONS_FILE = SERIALIZED_FOLDER + "/applications.ser";
    private static final String WITHDRAWALS_FILE = SERIALIZED_FOLDER + "/withdrawals.ser";

    /** 
     * @return List<User>
     */
    public List<User> loadUsers() {
        List<User> saved = loadSavedUsers();
        if (!saved.isEmpty()) {
            System.out.println("loaded users from saved data (" + USERS_FILE + ")");
            return saved;
        }

        System.out.println("no saved user data found. loading from csv...");
        List<User> users = loadUsersFromCSV(DATA_FOLDER);
        saveUsers(users);
        return users;
    }

    /** 
     * @param users
     */
    public void saveUsers(List<User> users) {
        ensureFolder(SERIALIZED_FOLDER);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            out.writeObject(users);
            System.out.println("saved users to " + new File(USERS_FILE));
        } catch (IOException e) {
            System.err.println("error saving users: " + e.getMessage());
        }
    }

    /** 
     * @return List<WithdrawalRequest>
     */
    public List<WithdrawalRequest> loadWithdrawalRequests() {
        List<WithdrawalRequest> saved = loadSavedWithdrawals();
        if (!saved.isEmpty()) {
            System.out.println("loaded withdrawal requests from saved data (" + WITHDRAWALS_FILE + ")");
            return saved;
        }

        System.out.println("no saved withdrawal request data found. returning empty list...");
        return new ArrayList<>();
    }

    /** 
     * @param requests
     */
    public void saveWithdrawalRequests(List<WithdrawalRequest> requests) {
        ensureFolder(SERIALIZED_FOLDER);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(WITHDRAWALS_FILE))) {
            out.writeObject(requests);
            System.out.println("saved withdrawal requests to " + new File(WITHDRAWALS_FILE));
        } catch (IOException e) {
            System.err.println("error saving withdrawal requests: " + e.getMessage());
        }
    }

    /** 
     * @param folderPath
     */
    private void ensureFolder(String folderPath) {
        File dir = new File(folderPath);
        if (!dir.exists()) dir.mkdirs();
    }

    /** 
     * @param folder
     * @return List<User>
     */
    private List<User> loadUsersFromCSV(String folder) {
        List<User> users = new ArrayList<>();
        try {
            users.addAll(loadStudents(folder + "/sample_student_list.csv"));
            users.addAll(loadStaff(folder + "/sample_staff_list.csv"));
            users.addAll(loadCompanyReps(folder + "/sample_company_representative_list.csv"));
        } catch (IOException e) {
            System.err.println("data load error: " + e.getMessage());
        }
        return users;
    }

    /** 
     * @param path
     * @return List<Student>
     * @throws IOException
     */
    private List<Student> loadStudents(String path) throws IOException {
        List<Student> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            br.readLine(); 
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] t = line.split(",", -1);
                if (t.length < 5) continue;

                String id    = t[0].trim();
                String name  = t[1].trim();
                String major = t[2].trim();

                int year;
                try {
                    year = Integer.parseInt(t[3].trim());
                } catch (NumberFormatException ex) {
                    continue;
                }
                Major majorEnum = parseMajor(major);

                Student s = new Student(id, name, year, majorEnum);
                s.setFirstLogin(true);
                list.add(s);
            }
        }
        return list;
    }

    /** 
     * @param path
     * @return List<CareerCenterStaff>
     * @throws IOException
     */
    private List<CareerCenterStaff> loadStaff(String path) throws IOException {
        List<CareerCenterStaff> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            br.readLine(); 
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] t = line.split(",", -1);
                if (t.length < 5) continue;

                String id         = t[0].trim();
                String name       = t[1].trim();
                String department = t[3].trim();

                CareerCenterStaff c = new CareerCenterStaff(id, name, department);
                c.setFirstLogin(true);
                list.add(c);
            }
        }
        return list;
    }

    /** 
     * @param path
     * @return List<CompanyRepresentative>
     * @throws IOException
     */
    private List<CompanyRepresentative> loadCompanyReps(String path) throws IOException {
        List<CompanyRepresentative> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            br.readLine(); 
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] t = line.split(",", -1);
                if (t.length < 7) continue;

                String id          = t[0].trim();
                String name        = t[1].trim();
                String companyName = t[2].trim();
                String department  = t[3].trim();
                String position    = t[4].trim();

                AccountStatus status;
                try {
                    status = AccountStatus.valueOf(t[6].trim().toUpperCase());
                } catch (IllegalArgumentException ex) {
                    status = AccountStatus.PENDING;
                }

                CompanyRepresentative cr = new CompanyRepresentative(id, name, companyName, department, position, status);
                cr.setFirstLogin(true);
                list.add(cr);
            }
        }
        return list;
    }

    /** 
     * @return List<InternshipOpportunity>
     */
    public List<InternshipOpportunity> loadOpportunities() {
        List<InternshipOpportunity> saved = loadSavedOpportunities();
        if (!saved.isEmpty()) {
            System.out.println("loaded opportunities from saved data (" + new File(OPPORTUNITIES_FILE) + ")");
            return saved;
        }
        System.out.println("no saved opportunity data found. returning empty list.");
        return new ArrayList<>();
    }

    /** 
     * @param list
     */
    public void saveOpportunities(List<InternshipOpportunity> list) {
        ensureFolder(SERIALIZED_FOLDER);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(OPPORTUNITIES_FILE))) {
            out.writeObject(list);
            System.out.println("saved opportunities to " + new File(OPPORTUNITIES_FILE));
        } catch (IOException e) {
            System.err.println("error saving opportunities: " + e.getMessage());
        }
    }

    /** 
     * @return List<Application>
     */
    public List<Application> loadApplications() {
        List<Application> saved = loadSavedApplications();
        if (!saved.isEmpty()) {
            System.out.println("loaded applications from saved data (" + new File(APPLICATIONS_FILE) + ")");
            return saved;
        }
        System.out.println("no saved application data found. returning empty list.");
        return new ArrayList<>();
    }

    /** 
     * @param list
     */
    public void saveApplications(List<Application> list) {
        ensureFolder(SERIALIZED_FOLDER);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(APPLICATIONS_FILE))) {
            out.writeObject(list);
            System.out.println("saved applications to " + new File(APPLICATIONS_FILE));
        } catch (IOException e) {
            System.err.println("error saving applications: " + e.getMessage());
        }
    }

    /** 
     * @param s
     * @return Major
     */
    private Major parseMajor(String s) {
        s = s.trim().toUpperCase();

        switch (s) {
            case "COMPUTER SCIENCE", "CSC" -> { return Major.CSC; }
            case "DATA SCIENCE & AI", "DSAI" -> { return Major.DSAI; }
            case "COMPUTER ENGINEERING", "CEG" -> { return Major.CEG; }
            case "INFORMATION ENGINEERING & MEDIA", "IEM" -> { return Major.IEM; }
            case "BUSINESS & COMPUTER SCIENCE", "BCG" -> { return Major.BCG; }
            case "BUSINESS & COMPUTER ENGINEERING", "BCE" -> { return Major.BCE; }
             default -> throw new IllegalArgumentException("invalid major: " + s);
        }
    }

    /** 
     * @return List<User>
     */
    @SuppressWarnings("unchecked")
    private List<User> loadSavedUsers() {
        File f = new File(USERS_FILE);
        if (!f.exists() || f.length() == 0) return new ArrayList<>();
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
            Object obj = in.readObject();
            if (obj instanceof List<?>) {
                return (List<User>) obj;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("error loading saved users: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    /** 
     * @return List<InternshipOpportunity>
     */
    @SuppressWarnings("unchecked")
    private List<InternshipOpportunity> loadSavedOpportunities() {
        File f = new File(OPPORTUNITIES_FILE);
        if (!f.exists() || f.length() == 0) return new ArrayList<>();
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
            Object obj = in.readObject();
            if (obj instanceof List<?>) {
                return (List<InternshipOpportunity>) obj;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("error loading saved opportunities: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    /** 
     * @return List<Application>
     */
    @SuppressWarnings("unchecked")
    private List<Application> loadSavedApplications() {
        File f = new File(APPLICATIONS_FILE);
        if (!f.exists() || f.length() == 0) return new ArrayList<>();
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
            Object obj = in.readObject();
            if (obj instanceof List<?>) {
                return (List<Application>) obj;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("error loading saved applications: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    /** 
     * @return List<WithdrawalRequest>
     */
    @SuppressWarnings("unchecked")
    private List<WithdrawalRequest> loadSavedWithdrawals() {
        File f = new File(WITHDRAWALS_FILE);
        if (!f.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
            Object obj = in.readObject();
            if (obj instanceof List<?>) {
                return (List<WithdrawalRequest>) obj;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("error loading withdrawals: " + e.getMessage());
        }

        return new ArrayList<>();
    }
}