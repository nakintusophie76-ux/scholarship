package com.scholar.service;

import com.scholar.model.Application;
import com.scholar.model.ApplicationStatus;
import com.scholar.model.Scholarship;
import com.scholar.model.Student;
import com.scholar.store.Csv;
import com.scholar.store.DataStore;
import com.scholar.util.Dates;
import com.scholar.util.Money;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Read-only reporting and CSV export.
 */
public final class ReportService {

    private final DataStore store;
    private final ApplicationService applications;

    public ReportService(DataStore store, ApplicationService applications) {
        this.store = store;
        this.applications = applications;
    }

    public record ScholarshipReport(Scholarship scholarship, int approved,
                                    int remaining, double awarded) {
    }

    public List<ScholarshipReport> awardsByScholarship() {
        return store.scholarships().stream()
                .sorted(Comparator.comparing(Scholarship::name, String.CASE_INSENSITIVE_ORDER))
                .map(s -> {
                    int approved = (int) applications.approvedCount(s.id());
                    return new ScholarshipReport(s, approved,
                            Math.max(0, s.slots() - approved), approved * s.amount());
                })
                .toList();
    }

    public int exportStudents(Path file) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("Student ID,First Name,Last Name,Email,Phone,Programme,Year,GPA,"
                + "Household Income,Status");
        for (Student s : store.students()) {
            lines.add(Csv.line(s.id(), s.firstName(), s.lastName(), s.email(), s.phone(),
                    s.program(), Integer.toString(s.year()), Money.plain(s.gpa()),
                    Money.plain(s.householdIncome()), s.status().label()));
        }
        write(file, lines);
        return lines.size() - 1;
    }

    public int exportApplications(Path file) throws IOException {
        Map<String, Student> students = applications.studentMap();
        Map<String, Scholarship> scholarships = applications.scholarshipMap();

        List<String> lines = new ArrayList<>();
        lines.add("Application ID,Student ID,Student,Scholarship,Type,Award,Submitted,Status,"
                + "Review Notes");
        for (Application a : applications.all()) {
            Student student = students.get(a.studentId());
            Scholarship scholarship = scholarships.get(a.scholarshipId());
            lines.add(Csv.line(a.id(), a.studentId(),
                    student == null ? "Unknown" : student.fullName(),
                    scholarship == null ? "Unknown" : scholarship.name(),
                    scholarship == null ? "" : scholarship.type().label(),
                    scholarship == null ? "" : Money.plain(scholarship.amount()),
                    Dates.format(a.submittedAt()), a.status().label(), a.reviewNotes()));
        }
        write(file, lines);
        return lines.size() - 1;
    }

    public int exportAwards(Path file) throws IOException {
        Map<String, Student> students = applications.studentMap();
        Map<String, Scholarship> scholarships = applications.scholarshipMap();

        List<String> lines = new ArrayList<>();
        lines.add("Application ID,Student ID,Student,Scholarship,Award,Approved On");
        int count = 0;
        for (Application a : applications.all()) {
            if (a.status() != ApplicationStatus.APPROVED) {
                continue;
            }
            Student student = students.get(a.studentId());
            Scholarship scholarship = scholarships.get(a.scholarshipId());
            lines.add(Csv.line(a.id(), a.studentId(),
                    student == null ? "Unknown" : student.fullName(),
                    scholarship == null ? "Unknown" : scholarship.name(),
                    scholarship == null ? "" : Money.plain(scholarship.amount()),
                    Dates.format(a.reviewedAt())));
            count++;
        }
        write(file, lines);
        return count;
    }

    private static void write(Path file, List<String> lines) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}
