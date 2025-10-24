package control;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import entity.*;

public class DataLoader {

	private static final String DEFAULT_PASSWORD = "password";
    private static final String DEFAULT_FOLDER = "data";
	
	public List<User> loadUsers() {
		List<User> users = new ArrayList<>();

        try {
            users.addAll(loadStudents(DEFAULT_FOLDER + "/sample_student_list.csv"));
            users.addAll(loadStaff(DEFAULT_FOLDER + "/sample_staff_list.csv"));
            users.addAll(loadCompanyReps(DEFAULT_FOLDER + "/sample_company_representative_list.csv"));
        } catch (IOException e) {
            System.err.println("Data load error: " + e.getMessage());
        }

        return users;
	}

	private List<Student> loadStudents(String path) throws IOException {
        List<Student> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            br.readLine(); // header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] t = line.split(",", -1);
                if (t.length < 5) continue;

                String id = t[0].trim();
                String name = t[1].trim();
                String major = t[2].trim();
                int year = Integer.parseInt(t[3].trim());

                list.add(new Student(id, name, DEFAULT_PASSWORD, year, major));
            }
        }
        return list;
    }

    private List<CareerCenterStaff> loadStaff(String path) throws IOException {
        List<CareerCenterStaff> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            br.readLine(); // header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] t = line.split(",", -1);
                if (t.length < 5) continue;

                String id = t[0].trim();
                String name = t[1].trim();
                String department = t[3].trim();

                list.add(new CareerCenterStaff(id, name, DEFAULT_PASSWORD, department));
            }
        }
        return list;
    }

	private List<CompanyRepresentative> loadCompanyReps(String path) throws IOException {
        List<CompanyRepresentative> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            br.readLine(); // header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] t = line.split(",", -1);
                if (t.length < 7) continue;

                String id = t[0].trim();
                String name = t[1].trim();
                String companyName = t[2].trim();
                String department = t[3].trim();
                String position = t[4].trim();

                AccountStatus status;
                try { status = AccountStatus.valueOf(t[6].trim().toUpperCase()); }
                catch (IllegalArgumentException ex) { status = AccountStatus.PENDING; }

                list.add(new CompanyRepresentative(id, name, DEFAULT_PASSWORD, companyName, department, position, status));
            }
        }
        return list;
    }
}