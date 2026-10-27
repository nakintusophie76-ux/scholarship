package com.scholar.service;

import com.scholar.model.Student;
import com.scholar.model.StudentStatus;
import com.scholar.store.DataStore;
import com.scholar.util.Ids;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Student records: validation, uniqueness and safe deletion.
 */
public final class StudentService {

    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final DataStore store;

    public StudentService(DataStore store) {
        this.store = store;
    }

    public List<Student> all() {
        return store.students().stream()
                .sorted(Comparator.comparing(Student::lastName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Student::firstName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public Student find(String id) {
        return store.findStudent(id);
    }

    public long count(StudentStatus status) {
        return store.students().stream().filter(s -> s.status() == status).count();
    }

    public String nextId() {
        return Ids.next("STU", store.students().stream().map(Student::id).toList());
    }

    /**
     * Creates a new student when the identifier is blank, otherwise updates
     * the existing record.
     */
    public Student save(Student input) throws ValidationException {
        String firstName = clean(input.firstName());
        String lastName = clean(input.lastName());
        String email = clean(input.email());
        String phone = clean(input.phone());
        String program = clean(input.program());

        if (firstName.isEmpty() || lastName.isEmpty()) {
            throw new ValidationException("First name and last name are required.");
        }
        if (email.isEmpty() || !EMAIL.matcher(email).matches()) {
            throw new ValidationException("Enter a valid email address, for example student@example.edu.");
        }
        if (program.isEmpty()) {
            throw new ValidationException("Programme / course is required.");
        }
        if (input.year() < 1 || input.year() > 6) {
            throw new ValidationException("Year of study must be between 1 and 6.");
        }
        if (input.gpa() < 0 || input.gpa() > 4.0) {
            throw new ValidationException("GPA must be between 0.00 and 4.00.");
        }
        if (input.householdIncome() < 0) {
            throw new ValidationException("Household income cannot be negative.");
        }

        String id = clean(input.id());
        if (id.isEmpty()) {
            id = nextId();
        }
        final String resolvedId = id;

        boolean duplicateEmail = store.students().stream()
                .anyMatch(s -> !s.id().equals(resolvedId) && s.email().equalsIgnoreCase(email));
        if (duplicateEmail) {
            throw new ValidationException("Another student already uses the email " + email + ".");
        }

        Student student = new Student(resolvedId, firstName, lastName, email, phone, program,
                input.year(), round2(input.gpa()), round2(input.householdIncome()),
                input.status() == null ? StudentStatus.ACTIVE : input.status());
        store.saveStudent(student);
        return student;
    }

    /**
     * Lets a student update the contact details on their own record. Academic
     * and household information can only be changed by the scholarship office.
     */
    public Student updateContact(String studentId, String email, String phone)
            throws ValidationException {

        Student existing = store.findStudent(studentId);
        if (existing == null) {
            throw new ValidationException("This student record no longer exists.");
        }

        String cleanedEmail = clean(email);
        String cleanedPhone = clean(phone);
        if (cleanedEmail.isEmpty() || !EMAIL.matcher(cleanedEmail).matches()) {
            throw new ValidationException("Enter a valid email address, for example student@example.edu.");
        }

        boolean duplicateEmail = store.students().stream()
                .anyMatch(s -> !s.id().equals(existing.id())
                        && s.email().equalsIgnoreCase(cleanedEmail));
        if (duplicateEmail) {
            throw new ValidationException("Another student already uses the email "
                    + cleanedEmail + ".");
        }

        Student updated = new Student(existing.id(), existing.firstName(), existing.lastName(),
                cleanedEmail, cleanedPhone, existing.program(), existing.year(), existing.gpa(),
                existing.householdIncome(), existing.status());
        store.saveStudent(updated);
        return updated;
    }

    /**
     * A student with applications cannot be removed, otherwise the application
     * history would point at a record that no longer exists.
     */
    public void delete(String studentId) throws ValidationException {
        Student student = store.findStudent(studentId);
        if (student == null) {
            throw new ValidationException("This student no longer exists.");
        }
        long applications = store.applications().stream()
                .filter(a -> a.studentId().equals(studentId))
                .count();
        if (applications > 0) {
            throw new ValidationException("Cannot delete " + student.fullName() + " because "
                    + applications + " application(s) reference this student.\n"
                    + "Delete those applications first.");
        }
        store.deleteStudent(studentId);
    }

    /** Case-insensitive search across name, email, programme and id. */
    public List<Student> search(String query, StudentStatus status) {
        String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return all().stream()
                .filter(s -> status == null || s.status() == status)
                .filter(s -> needle.isEmpty()
                        || s.fullName().toLowerCase(Locale.ROOT).contains(needle)
                        || s.email().toLowerCase(Locale.ROOT).contains(needle)
                        || s.program().toLowerCase(Locale.ROOT).contains(needle)
                        || s.id().toLowerCase(Locale.ROOT).contains(needle))
                .toList();
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
