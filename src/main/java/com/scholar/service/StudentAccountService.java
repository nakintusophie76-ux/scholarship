package com.scholar.service;

import com.scholar.model.Student;
import com.scholar.model.StudentAccount;
import com.scholar.model.StudentStatus;
import com.scholar.store.DataStore;
import com.scholar.util.Ids;
import com.scholar.util.Passwords;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Self-service student portal accounts: registration, sign in and password
 * management. Allows new students to create accounts and save their details.
 */
public final class StudentAccountService {

    private static final Pattern USERNAME =
            Pattern.compile("^[a-z0-9._-]{3,20}$");

    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final DataStore store;

    public StudentAccountService(DataStore store) {
        this.store = store;
    }

    public StudentAccount register(String studentId, String email, String username,
                                   String password, String confirmPassword)
            throws ValidationException {

        String id = clean(studentId);
        String address = clean(email);
        String name = clean(username).toLowerCase(Locale.ROOT);

        if (id.isEmpty() || address.isEmpty()) {
            throw new ValidationException("Enter your student ID and the email address on file.");
        }

        Student student = store.students().stream()
                .filter(s -> s.id().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
        if (student == null || !student.email().equalsIgnoreCase(address)) {
            throw new ValidationException(
                    "The student ID and email address do not match our records.\n"
                            + "Check your details or contact the scholarship office.");
        }
        if (store.findStudentAccountByStudentId(student.id()) != null) {
            throw new ValidationException("A portal account already exists for " + student.fullName()
                    + ".\nSign in with your username or contact the office if you need help.");
        }
        if (!USERNAME.matcher(name).matches()) {
            throw new ValidationException("Username must be 3 to 20 characters and may only contain "
                    + "letters, numbers, dots, dashes and underscores.");
        }
        if (store.findStudentAccount(name) != null || store.findUser(name) != null) {
            throw new ValidationException("That username is already in use. Choose another one.");
        }
        validateNewPassword(password, confirmPassword);

        String salt = Passwords.newSalt();
        StudentAccount account = new StudentAccount(student.id(), name, salt,
                Passwords.hash(password, salt));
        store.saveStudentAccount(account);
        return account;
    }

    public StudentAccount register(String studentId, String email, String username,
                                   String password, String confirmPassword,
                                   String firstName, String lastName, String phone,
                                   String program, Integer year, Double gpa,
                                   Double householdIncome, StudentStatus status)
            throws ValidationException {

        String id = clean(studentId);
        String address = clean(email);
        String name = clean(username).toLowerCase(Locale.ROOT);

        if (address.isEmpty() || !EMAIL.matcher(address).matches()) {
            throw new ValidationException("Enter a valid email address.");
        }

        if (name.isEmpty()) {
            throw new ValidationException("Enter a username.");
        }

        if (!USERNAME.matcher(name).matches()) {
            throw new ValidationException("Username must be 3 to 20 characters and may only contain "
                    + "letters, numbers, dots, dashes and underscores.");
        }
        if (store.findStudentAccount(name) != null || store.findUser(name) != null) {
            throw new ValidationException("That username is already in use. Choose another one.");
        }

        validateNewPassword(password, confirmPassword);

        Student student;
        if (id.isEmpty()) {
            String first = clean(firstName);
            String last = clean(lastName);
            String prog = clean(program);
            if (first.isEmpty() || last.isEmpty()) {
                throw new ValidationException("Enter first name and last name.");
            }
            if (prog.isEmpty()) {
                throw new ValidationException("Enter your programme/course.");
            }
            int y = year == null ? 1 : year;
            if (y < 1 || y > 6) {
                throw new ValidationException("Year of study must be between 1 and 6.");
            }
            double g = gpa == null ? 0.0 : gpa;
            if (g < 0 || g > 4.0) {
                throw new ValidationException("GPA must be between 0.00 and 4.00.");
            }
            double income = householdIncome == null ? 0.0 : householdIncome;
            if (income < 0) {
                throw new ValidationException("Household income cannot be negative.");
            }

            boolean duplicateEmail = store.students().stream()
                    .anyMatch(s -> s.email().equalsIgnoreCase(address));
            if (duplicateEmail) {
                throw new ValidationException("Another student already uses the email " + address + ".");
            }

            String resolvedId = Ids.next("STU", store.students().stream().map(Student::id).toList());
            student = new Student(resolvedId, first, last, address, clean(phone), prog,
                    y, Math.round(g * 100.0) / 100.0, Math.round(income * 100.0) / 100.0,
                    status == null ? StudentStatus.ACTIVE : status);
            store.saveStudent(student);
        } else {
            student = store.students().stream()
                    .filter(s -> s.id().equalsIgnoreCase(id))
                    .findFirst()
                    .orElse(null);
            if (student == null) {
                throw new ValidationException(
                        "Student ID " + id + " was not found. Leave Student ID blank to create a new record.");
            }
            if (!student.email().equalsIgnoreCase(address)) {
                throw new ValidationException(
                        "The student ID and email address do not match our records.\n"
                                + "Check your details or leave Student ID blank to create a new student record.");
            }
        }

        if (store.findStudentAccountByStudentId(student.id()) != null) {
            throw new ValidationException("A portal account already exists for " + student.fullName()
                    + ".\nSign in with your username or contact the office if you need help.");
        }

        String salt = Passwords.newSalt();
        StudentAccount account = new StudentAccount(student.id(), name, salt,
                Passwords.hash(password, salt));
        store.saveStudentAccount(account);
        return account;
    }

    public StudentAccount login(String username, String password) throws ValidationException {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            throw new ValidationException("Enter your username and password.");
        }
        StudentAccount account = store.findStudentAccount(username);
        if (account == null || !Passwords.matches(password, account.salt(), account.passwordHash())) {
            throw new ValidationException("Invalid username or password.");
        }
        if (store.findStudent(account.studentId()) == null) {
            throw new ValidationException(
                    "This portal account is no longer linked to a student record.\n"
                            + "Contact the scholarship office for help.");
        }
        return account;
    }

    public StudentAccount changePassword(StudentAccount account, String currentPassword,
                                         String newPassword, String confirmPassword)
            throws ValidationException {

        if (account == null) {
            throw new ValidationException("You are not signed in.");
        }
        if (!Passwords.matches(currentPassword, account.salt(), account.passwordHash())) {
            throw new ValidationException("The current password is incorrect.");
        }
        validateNewPassword(newPassword, confirmPassword);
        if (newPassword.equals(currentPassword)) {
            throw new ValidationException("Choose a password different from the current one.");
        }

        String salt = Passwords.newSalt();
        StudentAccount updated = account.withPassword(salt, Passwords.hash(newPassword, salt));
        store.saveStudentAccount(updated);
        return updated;
    }

    public StudentAccount findForStudent(String studentId) {
        return store.findStudentAccountByStudentId(studentId);
    }

    public boolean hasAccount(String studentId) {
        return store.findStudentAccountByStudentId(studentId) != null;
    }

    private static void validateNewPassword(String password, String confirmPassword)
            throws ValidationException {
        if (password == null || password.length() < 6) {
            throw new ValidationException("The password must have at least 6 characters.");
        }
        if (!password.equals(confirmPassword)) {
            throw new ValidationException("The passwords do not match.");
        }
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
