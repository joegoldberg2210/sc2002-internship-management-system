package control;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import entity.*;
import enumerations.AccountStatus;
import enumerations.InternshipLevel;

public class DataLoader {

    // keep csv inputs and serialized outputs in different folders
    private static final String DATA_FOLDER       = "data";
    private static final String SERIALIZED_FOLDER = "serialized";

    private static final String USERS_FILE         = SERIALIZED_FOLDER + "/users.ser";
    private static final String OPPORTUNITIES_FILE = SERIALIZED_FOLDER + "/opportunities.ser";

    /* load saved users if available; else load from csv and save */
    public List<User> loadUsers() {
        List<User> saved = loadSavedUsers();
        if (!saved.isEmpty()) {
            System.out.println("loaded users from saved data (" + USERS_FILE + ")");
            return saved;
        }

        System.out.println("no saved user data found. loading from csv...");
        List<User> users = loadUsersFromCsv(DATA_FOLDER);
        saveUsers(users);
        return users;
    }

    /* write all users (students, staff, reps — including pending/approved/rejected) */
    public void saveUsers(List<User> users) {
        ensureFolder(SERIALIZED_FOLDER);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            out.writeObject(users);
            System.out.println("saved users to " + new File(USERS_FILE).getAbsolutePath());
        } catch (IOException e) {
            System.err.println("error saving users: " + e.getMessage());
        }
    }

    /* opportunities: prefer .ser; if missing, fall back to csv, then save */
    public List<InternshipOpportunity> loadOpportunities() {
        List<InternshipOpportunity> saved = loadSavedOpportunities();
        if (!saved.isEmpty()) return saved;

        System.out.println("no saved opportunity data found. loading from csv...");
        List<InternshipOpportunity> list = loadOpportunitiesFromCsv(DATA_FOLDER + "/sample_internship_opportunities.csv");
        saveOpportunities(list);
        return list;
    }

    public void saveOpportunities(List<InternshipOpportunity> list) {
        ensureFolder(SERIALIZED_FOLDER);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(OPPORTUNITIES_FILE))) {
            out.writeObject(list);
            System.out.println("saved opportunities to " + new File(OPPORTUNITIES_FILE).getAbsolutePath());
        } catch (IOException e) {
            System.err.println("error saving opportunities: " + e.getMessage());
        }
    }

    /* ===== private helpers ===== */

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

    @SuppressWarnings("unchecked")
    private List<InternshipOpportunity> loadSavedOpportunities() {
        File f = new File(OPPORTUNITIES_FILE);
        if (!f.exists() || f.length() == 0) return new ArrayList<>();
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
            Object obj = in.readObject();
            if (obj instanceof List<?>) {
                System.out.println("loaded opportunities from " + f.getAbsolutePath());
                return (List<InternshipOpportunity>) obj;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("error loading saved opportunities: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    private void ensureFolder(String folderPath) {
        File dir = new File(folderPath);
        if (!dir.exists()) dir.mkdirs();
    }

    /* build users from csv files (students, staff, company reps) */
    private List<User> loadUsersFromCsv(String folder) {
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

    // load internship opportunities from csv (default status handled in entity constructor)
    private List<InternshipOpportunity> loadOpportunitiesFromCsv(String path) {
        List<InternshipOpportunity> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] t = line.split(",", -1);
                if (t.length < 9) continue;

                String id          = t[0].trim();
                String title       = t[1].trim();
                String description = t[2].trim();
                InternshipLevel level = InternshipLevel.valueOf(t[3].trim().toUpperCase());
                String major      = t[4].trim();
                LocalDate open    = LocalDate.parse(t[5].trim());
                LocalDate close   = LocalDate.parse(t[6].trim());
                String company    = t[7].trim();
                int slots         = Integer.parseInt(t[8].trim());

                InternshipOpportunity opp = new InternshipOpportunity(
                        id, title, description, level, major, open, close, company, slots, null
                );
                // status defaults to PENDING in your constructor; visibility=false
                list.add(opp);
            }
            System.out.println("loaded " + list.size() + " internship opportunities from csv.");
        } catch (IOException e) {
            System.out.println("⚠️ error reading opportunities file: " + e.getMessage());
        }
        return list;
    }

    /* csv: students */
    private List<Student> loadStudents(String path) throws IOException {
        List<Student> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            br.readLine(); // header
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

                list.add(new Student(id, name, year, major));
            }
        }
        return list;
    }

    /* csv: staff */
    private List<CareerCenterStaff> loadStaff(String path) throws IOException {
        List<CareerCenterStaff> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            br.readLine(); // header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] t = line.split(",", -1);
                if (t.length < 5) continue;

                String id         = t[0].trim();
                String name       = t[1].trim();
                String department = t[3].trim();

                list.add(new CareerCenterStaff(id, name, department));
            }
        }
        return list;
    }

    /* csv: company representatives (status column optional; defaults to PENDING) */
    private List<CompanyRepresentative> loadCompanyReps(String path) throws IOException {
        List<CompanyRepresentative> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            br.readLine(); // header
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

                list.add(new CompanyRepresentative(id, name, companyName, department, position, status));
            }
        }
        return list;
    }
}