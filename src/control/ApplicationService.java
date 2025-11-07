package control;

import java.util.*;
import entity.Application;
import entity.InternshipOpportunity;
import entity.Student;

public class ApplicationService {
    private static final int MAX_ACTIVE_APPS = 3;

    private final List<Application> applications;
    private final DataLoader loader;

    public ApplicationService(List<Application> applications, DataLoader loader) {
        this.applications = Objects.requireNonNull(applications);
        this.loader = Objects.requireNonNull(loader);
    }

    /** student applies for an opportunity; returns the created application or null on failure */
    public Application applyForOpportunity(Student student, InternshipOpportunity opp) {
        if (student == null || opp == null) return null;

        // must be visible, within window, have vacancy, and match student's major/level
        if (!opp.isOpenFor(student)) return null;

        // cap at 3 active applications
        if (getActiveCountForStudent(student.getId()) >= MAX_ACTIVE_APPS) return null;

        // no duplicate active application for the same internship
        if (hasActiveApplication(student, opp)) return null;

        // create + persist
        String appId = "APP-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Application app = new Application(appId, student, opp); // defaults to PENDING
        applications.add(app);
        save();
        return app;
    }

    /** public: count student's active (pending) applications */
    public long getActiveCountForStudent(String studentId) {
        return applications.stream()
                .filter(a -> a.getStudent().getId().equalsIgnoreCase(studentId) && a.isActive())
                .count();
    }

    /** public: does student already have an active app for this exact opportunity? */
    public boolean hasActiveApplication(Student student, InternshipOpportunity opp) {
        return applications.stream().anyMatch(a ->
                a.getStudent().getId().equalsIgnoreCase(student.getId())
                        && a.getOpportunity().getId().equalsIgnoreCase(opp.getId())
                        && a.isActive());
    }

    /** public: fetch all applications for this student */
    public List<Application> getApplicationsForStudent(Student student) {
        return applications.stream()
                .filter(a -> a.getStudent().getId().equalsIgnoreCase(student.getId()))
                .toList();
    }

    private void save() {
        loader.saveApplications(applications);
    }
}