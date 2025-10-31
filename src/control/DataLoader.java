package control;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import entity.*;
import enumerations.AccountStatus;
import enumerations.InternshipLevel;

public class DataLoader {

    private static final String DEFAULT_FOLDER = "data";
    private static final String SAVE_FILE      = DEFAULT_FOLDER + "/users.ser";

    /* Load saved users if available; else load from csv and save */
    public List<User> loadUsers() {
        // 1) try saved data
        List<User> saved = loadSavedUsers();
        if (!saved.isEmpty()) {
            System.out.println("loaded users from saved data (" + SAVE_FILE + ")");
            return saved;
        }

        // 2) fallback to csv, then save for next run
        System.out.println("no saved data found. loading from csv...");
        List<User> users = loadUsers(DEFAULT_FOLDER);
        saveUsers(users);
        return users;
    }

    /* Load from CSV files */
    public List<User> loadUsers(String folder) {
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

    public void saveUsers(List<User> users) {
        // ensure folder exists
        File dir = new File(DEFAULT_FOLDER);
        if (!dir.exists()) dir.mkdirs();

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            out.writeObject(users);
            System.out.println("saved users to " + SAVE_FILE);
        } catch (IOException e) {
            System.err.println("error saving users: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private List<User> loadSavedUsers() {
        File f = new File(SAVE_FILE);
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

    // load students from data/sample_student_list.csv
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

                list.add(new Student(id, name, year, major)); // default password from User
            }
        }
        return list;
    }

    // load career center staff from data/sample_staff_list.csv
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

    //load internship opportunities

    public List<InternshipOpportunity> loadOpportunities() {
        List<InternshipOpportunity> list = new ArrayList<>();
        String path = "data/sample_internship_opportunities.csv";

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] t = line.split(",", -1);
                if (t.length < 9) continue;

                String id = t[0].trim();
                String title = t[1].trim();
                String description = t[2].trim();
                InternshipLevel level = InternshipLevel.valueOf(t[3].trim().toUpperCase());
                String major = t[4].trim();
                LocalDate openDate = LocalDate.parse(t[5].trim());
                LocalDate closeDate = LocalDate.parse(t[6].trim());
                String companyName = t[7].trim();
                int slots = Integer.parseInt(t[8].trim());

                InternshipOpportunity opp = new InternshipOpportunity(
                    id, title, description, level, major,
                    openDate, closeDate, companyName, slots, null
                );
                list.add(opp);
            }
            System.out.println("Loaded " + list.size() + " internship opportunities.");
        } catch (IOException e) {
            System.out.println("⚠️ Error reading opportunities file: " + e.getMessage());
        }
        return list;
    }



    // load career center staff from data/sample_company_representative_list.csv
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