package com.scholar.store;

import com.scholar.model.AdminUser;
import com.scholar.model.Application;
import com.scholar.model.ApplicationStatus;
import com.scholar.model.Scholarship;
import com.scholar.model.ScholarshipType;
import com.scholar.model.Student;
import com.scholar.model.StudentAccount;
import com.scholar.model.StudentStatus;
import com.scholar.util.Money;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * File backed data store. Every collection is kept in memory and flushed to a
 * CSV file after each change, which keeps the application simple while still
 * making the data easy to inspect and back up.
 */
public final class DataStore {

    public static final String STUDENTS_FILE = "students.csv";
    public static final String SCHOLARSHIPS_FILE = "scholarships.csv";
    public static final String APPLICATIONS_FILE = "applications.csv";
    public static final String USERS_FILE = "users.csv";
    public static final String STUDENT_ACCOUNTS_FILE = "student_accounts.csv";

    private final Path directory;
    private final List<Student> students = new ArrayList<>();
    private final List<Scholarship> scholarships = new ArrayList<>();
    private final List<Application> applications = new ArrayList<>();
    private final List<AdminUser> users = new ArrayList<>();
    private final List<StudentAccount> studentAccounts = new ArrayList<>();

    public DataStore(Path directory) {
        this.directory = Objects.requireNonNull(directory, "directory").toAbsolutePath();
    }

    /**
     * Loads existing files. On a clean install it writes a small demo data set
     * so the application is useful from the first launch.
     */
    public synchronized void initialize() {
        try {
            Files.createDirectories(directory);
            boolean firstRun = Files.notExists(directory.resolve(STUDENTS_FILE));
            if (firstRun) {
                SeedData.populate(students, scholarships, applications, users, studentAccounts);
                writeAll();
            } else {
                students.addAll(readStudents());
                scholarships.addAll(readScholarships());
                applications.addAll(readApplications());
                users.addAll(readUsers());
                studentAccounts.addAll(readStudentAccounts());
                if (users.isEmpty()) {
                    users.add(SeedData.defaultAdmin());
                    writeUsers();
                }
                if (Files.notExists(directory.resolve(STUDENT_ACCOUNTS_FILE))) {
                    writeStudentAccounts();
                }
            }
        } catch (IOException ex) {
            throw new DataAccessException("Could not initialize the data folder: " + directory, ex);
        }
    }

    public Path directory() {
        return directory;
    }

    // ------------------------------------------------------------------
    // Students
    // ------------------------------------------------------------------

    public synchronized List<Student> students() {
        return List.copyOf(students);
    }

    public synchronized Student findStudent(String id) {
        return students.stream().filter(s -> s.id().equals(id)).findFirst().orElse(null);
    }

    public synchronized void saveStudent(Student student) {
        upsert(students, student, Student::id);
        writeStudents();
    }

    public synchronized void deleteStudent(String id) {
        boolean removed = remove(students, s -> s.id().equals(id));
        boolean accountRemoved = remove(studentAccounts, a -> a.studentId().equals(id));
        if (removed) {
            writeStudents();
        }
        if (accountRemoved) {
            writeStudentAccounts();
        }
    }

    // ------------------------------------------------------------------
    // Scholarships
    // ------------------------------------------------------------------

    public synchronized List<Scholarship> scholarships() {
        return List.copyOf(scholarships);
    }

    public synchronized Scholarship findScholarship(String id) {
        return scholarships.stream().filter(s -> s.id().equals(id)).findFirst().orElse(null);
    }

    public synchronized void saveScholarship(Scholarship scholarship) {
        upsert(scholarships, scholarship, Scholarship::id);
        writeScholarships();
    }

    public synchronized void deleteScholarship(String id) {
        if (remove(scholarships, s -> s.id().equals(id))) {
            writeScholarships();
        }
    }

    // ------------------------------------------------------------------
    // Applications
    // ------------------------------------------------------------------

    public synchronized List<Application> applications() {
        return List.copyOf(applications);
    }

    public synchronized Application findApplication(String id) {
        return applications.stream().filter(a -> a.id().equals(id)).findFirst().orElse(null);
    }

    public synchronized void saveApplication(Application application) {
        upsert(applications, application, Application::id);
        writeApplications();
    }

    public synchronized void deleteApplication(String id) {
        if (remove(applications, a -> a.id().equals(id))) {
            writeApplications();
        }
    }

    // ------------------------------------------------------------------
    // Users
    // ------------------------------------------------------------------

    public synchronized AdminUser findUser(String username) {
        if (username == null) {
            return null;
        }
        return users.stream()
                .filter(u -> u.username().equalsIgnoreCase(username.trim()))
                .findFirst()
                .orElse(null);
    }

    public synchronized void saveUser(AdminUser user) {
        upsert(users, user, AdminUser::username);
        writeUsers();
    }

    // ------------------------------------------------------------------
    // Student accounts
    // ------------------------------------------------------------------

    public synchronized List<StudentAccount> studentAccounts() {
        return List.copyOf(studentAccounts);
    }

    public synchronized StudentAccount findStudentAccount(String username) {
        if (username == null) {
            return null;
        }
        return studentAccounts.stream()
                .filter(a -> a.username().equalsIgnoreCase(username.trim()))
                .findFirst()
                .orElse(null);
    }

    public synchronized StudentAccount findStudentAccountByStudentId(String studentId) {
        if (studentId == null) {
            return null;
        }
        return studentAccounts.stream()
                .filter(a -> a.studentId().equalsIgnoreCase(studentId.trim()))
                .findFirst()
                .orElse(null);
    }

    public synchronized void saveStudentAccount(StudentAccount account) {
        upsert(studentAccounts, account, StudentAccount::studentId);
        writeStudentAccounts();
    }

    public synchronized void deleteStudentAccount(String studentId) {
        if (remove(studentAccounts, a -> a.studentId().equalsIgnoreCase(studentId))) {
            writeStudentAccounts();
        }
    }

    // ------------------------------------------------------------------
    // Serialization
    // ------------------------------------------------------------------

    private void writeAll() {
        writeStudents();
        writeScholarships();
        writeApplications();
        writeUsers();
        writeStudentAccounts();
    }

    private void writeStudents() {
        List<String> lines = new ArrayList<>();
        lines.add("id,firstName,lastName,email,phone,program,year,gpa,householdIncome,status");
        for (Student s : students) {
            lines.add(Csv.line(s.id(), s.firstName(), s.lastName(), s.email(), s.phone(),
                    s.program(), Integer.toString(s.year()), Money.plain(s.gpa()),
                    Money.plain(s.householdIncome()), s.status().name()));
        }
        writeLines(STUDENTS_FILE, lines);
    }

    private void writeScholarships() {
        List<String> lines = new ArrayList<>();
        lines.add("id,name,sponsor,type,amount,slots,minGpa,maxIncome,deadline,description,active");
        for (Scholarship s : scholarships) {
            lines.add(Csv.line(s.id(), s.name(), s.sponsor(), s.type().name(),
                    Money.plain(s.amount()), Integer.toString(s.slots()), Money.plain(s.minGpa()),
                    Money.plain(s.maxIncome()),
                    s.deadline() == null ? "" : s.deadline().toString(),
                    s.description(), Boolean.toString(s.active())));
        }
        writeLines(SCHOLARSHIPS_FILE, lines);
    }

    private void writeApplications() {
        List<String> lines = new ArrayList<>();
        lines.add("id,studentId,scholarshipId,submittedAt,status,statement,reviewNotes,reviewedAt");
        for (Application a : applications) {
            lines.add(Csv.line(a.id(), a.studentId(), a.scholarshipId(),
                    a.submittedAt() == null ? "" : a.submittedAt().toString(),
                    a.status().name(), a.statement(), a.reviewNotes(),
                    a.reviewedAt() == null ? "" : a.reviewedAt().toString()));
        }
        writeLines(APPLICATIONS_FILE, lines);
    }

    private void writeUsers() {
        List<String> lines = new ArrayList<>();
        lines.add("username,displayName,salt,passwordHash");
        for (AdminUser u : users) {
            lines.add(Csv.line(u.username(), u.displayName(), u.salt(), u.passwordHash()));
        }
        writeLines(USERS_FILE, lines);
    }

    private void writeStudentAccounts() {
        List<String> lines = new ArrayList<>();
        lines.add("studentId,username,salt,passwordHash");
        for (StudentAccount account : studentAccounts) {
            lines.add(Csv.line(account.studentId(), account.username(),
                    account.salt(), account.passwordHash()));
        }
        writeLines(STUDENT_ACCOUNTS_FILE, lines);
    }

    private List<Student> readStudents() {
        List<Student> result = new ArrayList<>();
        List<String> lines = readLines(STUDENTS_FILE);
        for (int i = 1; i < lines.size(); i++) {
            List<String> c = Csv.parse(lines.get(i));
            if (c.size() < 10 || Csv.text(c, 0).isBlank()) {
                continue;
            }
            result.add(new Student(Csv.text(c, 0), Csv.text(c, 1), Csv.text(c, 2),
                    Csv.text(c, 3), Csv.text(c, 4), Csv.text(c, 5),
                    Csv.integer(c, 6, 1), Csv.decimal(c, 7, 0), Csv.decimal(c, 8, 0),
                    enumValue(StudentStatus.class, Csv.text(c, 9), StudentStatus.ACTIVE)));
        }
        return result;
    }

    private List<Scholarship> readScholarships() {
        List<Scholarship> result = new ArrayList<>();
        List<String> lines = readLines(SCHOLARSHIPS_FILE);
        for (int i = 1; i < lines.size(); i++) {
            List<String> c = Csv.parse(lines.get(i));
            if (c.size() < 11 || Csv.text(c, 0).isBlank()) {
                continue;
            }
            result.add(new Scholarship(Csv.text(c, 0), Csv.text(c, 1), Csv.text(c, 2),
                    enumValue(ScholarshipType.class, Csv.text(c, 3), ScholarshipType.MERIT),
                    Csv.decimal(c, 4, 0), Csv.integer(c, 5, 1), Csv.decimal(c, 6, 0),
                    Csv.decimal(c, 7, 0), Csv.date(c, 8), Csv.text(c, 9), Csv.bool(c, 10, true)));
        }
        return result;
    }

    private List<Application> readApplications() {
        List<Application> result = new ArrayList<>();
        List<String> lines = readLines(APPLICATIONS_FILE);
        for (int i = 1; i < lines.size(); i++) {
            List<String> c = Csv.parse(lines.get(i));
            if (c.size() < 8 || Csv.text(c, 0).isBlank()) {
                continue;
            }
            result.add(new Application(Csv.text(c, 0), Csv.text(c, 1), Csv.text(c, 2),
                    Csv.dateTime(c, 3),
                    enumValue(ApplicationStatus.class, Csv.text(c, 4), ApplicationStatus.PENDING),
                    Csv.text(c, 5), Csv.text(c, 6), Csv.dateTime(c, 7)));
        }
        return result;
    }

    private List<AdminUser> readUsers() {
        List<AdminUser> result = new ArrayList<>();
        List<String> lines = readLines(USERS_FILE);
        for (int i = 1; i < lines.size(); i++) {
            List<String> c = Csv.parse(lines.get(i));
            if (c.size() < 4 || Csv.text(c, 0).isBlank()) {
                continue;
            }
            result.add(new AdminUser(Csv.text(c, 0), Csv.text(c, 1),
                    Csv.text(c, 2), Csv.text(c, 3)));
        }
        return result;
    }

    private List<StudentAccount> readStudentAccounts() {
        List<StudentAccount> result = new ArrayList<>();
        List<String> lines = readLines(STUDENT_ACCOUNTS_FILE);
        for (int i = 1; i < lines.size(); i++) {
            List<String> c = Csv.parse(lines.get(i));
            if (c.size() < 4 || Csv.text(c, 0).isBlank() || Csv.text(c, 1).isBlank()) {
                continue;
            }
            result.add(new StudentAccount(Csv.text(c, 0), Csv.text(c, 1),
                    Csv.text(c, 2), Csv.text(c, 3)));
        }
        return result;
    }

    private List<String> readLines(String fileName) {
        Path file = directory.resolve(fileName);
        if (Files.notExists(file)) {
            return List.of();
        }
        try {
            return Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new DataAccessException("Could not read " + file.getFileName(), ex);
        }
    }

    private void writeLines(String fileName, List<String> lines) {
        try {
            Files.createDirectories(directory);
            Path target = directory.resolve(fileName);
            Path temp = directory.resolve(fileName + ".tmp");
            Files.write(temp, lines, StandardCharsets.UTF_8);
            try {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new DataAccessException("Could not save " + fileName, ex);
        }
    }

    private static <T> void upsert(List<T> items, T value, Function<T, String> id) {
        String incoming = id.apply(value);
        for (int i = 0; i < items.size(); i++) {
            if (Objects.equals(id.apply(items.get(i)), incoming)) {
                items.set(i, value);
                return;
            }
        }
        items.add(value);
    }

    private static <T> boolean remove(List<T> items, Predicate<T> predicate) {
        return items.removeIf(predicate);
    }

    private static <E extends Enum<E>> E enumValue(Class<E> type, String value, E fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, value.trim());
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }
}
