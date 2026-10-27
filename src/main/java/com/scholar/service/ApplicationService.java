package com.scholar.service;

import com.scholar.model.Application;
import com.scholar.model.ApplicationStatus;
import com.scholar.model.Scholarship;
import com.scholar.model.Student;
import com.scholar.store.DataStore;
import com.scholar.util.Dates;
import com.scholar.util.Ids;
import com.scholar.util.Money;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Submitting and reviewing applications. This is where the scholarship rules
 * live, so the UI never has to guess whether an application is allowed.
 */
public final class ApplicationService {

    private final DataStore store;

    public ApplicationService(DataStore store) {
        this.store = store;
    }

    public List<Application> all() {
        return store.applications().stream()
                .sorted(Comparator.comparing(Application::submittedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public List<Application> recent(int limit) {
        return all().stream().limit(Math.max(0, limit)).toList();
    }

    /** Every application belonging to one student, newest first. */
    public List<Application> applicationsForStudent(String studentId) {
        return all().stream()
                .filter(a -> a.studentId().equals(studentId))
                .toList();
    }

    /** Total value of scholarships awarded to one student. */
    public double studentTotalAwarded(String studentId) {
        Map<String, Scholarship> scholarships = scholarshipMap();
        return store.applications().stream()
                .filter(a -> a.studentId().equals(studentId))
                .filter(a -> a.status() == ApplicationStatus.APPROVED)
                .mapToDouble(a -> {
                    Scholarship s = scholarships.get(a.scholarshipId());
                    return s == null ? 0 : s.amount();
                })
                .sum();
    }

    public Application find(String id) {
        return store.findApplication(id);
    }

    public String nextId() {
        return Ids.next("APP", store.applications().stream().map(Application::id).toList());
    }

    public long approvedCount(String scholarshipId) {
        return store.applications().stream()
                .filter(a -> a.scholarshipId().equals(scholarshipId))
                .filter(a -> a.status() == ApplicationStatus.APPROVED)
                .count();
    }

    public double totalAwarded() {
        Map<String, Scholarship> scholarships = scholarshipMap();
        return store.applications().stream()
                .filter(a -> a.status() == ApplicationStatus.APPROVED)
                .mapToDouble(a -> {
                    Scholarship s = scholarships.get(a.scholarshipId());
                    return s == null ? 0 : s.amount();
                })
                .sum();
    }

    public Map<ApplicationStatus, Long> statusCounts() {
        Map<ApplicationStatus, Long> counts = new EnumMap<>(ApplicationStatus.class);
        for (ApplicationStatus status : ApplicationStatus.values()) {
            counts.put(status, 0L);
        }
        for (Application application : store.applications()) {
            counts.merge(application.status(), 1L, Long::sum);
        }
        return counts;
    }

    /**
     * Returns every reason why the student may not apply. An empty list means
     * the application is allowed.
     */
    public List<String> eligibilityProblems(String studentId, String scholarshipId) {
        List<String> problems = new ArrayList<>();
        Student student = store.findStudent(studentId);
        Scholarship scholarship = store.findScholarship(scholarshipId);

        if (student == null) {
            problems.add("Select a student.");
        }
        if (scholarship == null) {
            problems.add("Select a scholarship.");
        }
        if (student == null || scholarship == null) {
            return problems;
        }

        if (student.status() != com.scholar.model.StudentStatus.ACTIVE) {
            problems.add("The student record is " + student.status().label().toLowerCase(Locale.ROOT)
                    + "; only active students can apply.");
        }
        if (!scholarship.active()) {
            problems.add("This scholarship has been archived and is no longer accepting applications.");
        }
        if (scholarship.deadline() != null && scholarship.deadline().isBefore(LocalDate.now())) {
            problems.add("The deadline passed on " + Dates.format(scholarship.deadline()) + ".");
        }
        if (student.gpa() < scholarship.minGpa()) {
            problems.add("GPA " + Money.gpa(student.gpa()) + " is below the minimum of "
                    + Money.gpa(scholarship.minGpa()) + ".");
        }
        if (scholarship.hasIncomeLimit() && student.householdIncome() > scholarship.maxIncome()) {
            problems.add("Household income " + Money.format(student.householdIncome())
                    + " is above the " + Money.format(scholarship.maxIncome()) + " limit.");
        }
        int remaining = scholarship.remainingSlots(approvedCount(scholarship.id()));
        if (remaining <= 0) {
            problems.add("All " + scholarship.slots() + " places have already been awarded.");
        }

        store.applications().stream()
                .filter(a -> a.studentId().equals(student.id())
                        && a.scholarshipId().equals(scholarship.id()))
                .findFirst()
                .ifPresent(existing -> problems.add("This student already has application "
                        + existing.id() + " for this scholarship."));

        return problems;
    }

    public Application apply(String studentId, String scholarshipId, String statement)
            throws ValidationException {
        List<String> problems = eligibilityProblems(studentId, scholarshipId);
        if (!problems.isEmpty()) {
            throw new ValidationException("This application cannot be submitted:\n• "
                    + String.join("\n• ", problems));
        }

        Application application = new Application(nextId(), studentId, scholarshipId,
                LocalDateTime.now().withNano(0), ApplicationStatus.PENDING,
                statement == null ? "" : statement.trim(), "", null);
        store.saveApplication(application);
        return application;
    }

    public Application review(String applicationId, ApplicationStatus status, String notes)
            throws ValidationException {
        Application current = store.findApplication(applicationId);
        if (current == null) {
            throw new ValidationException("This application no longer exists.");
        }
        if (status == null) {
            throw new ValidationException("Choose a decision.");
        }

        if (status == ApplicationStatus.APPROVED && current.status() != ApplicationStatus.APPROVED) {
            Scholarship scholarship = store.findScholarship(current.scholarshipId());
            if (scholarship == null) {
                throw new ValidationException("The linked scholarship no longer exists.");
            }
            long approved = approvedCount(scholarship.id());
            if (approved >= scholarship.slots()) {
                throw new ValidationException("All " + scholarship.slots()
                        + " places for this scholarship are already awarded.\n"
                        + "Waitlist this application or move another awardee to the waitlist first.");
            }
        }

        LocalDateTime reviewedAt = status == ApplicationStatus.PENDING
                ? null : LocalDateTime.now().withNano(0);
        Application updated = current.withStatus(status,
                notes == null ? "" : notes.trim(), reviewedAt);
        store.saveApplication(updated);
        return updated;
    }

    public void delete(String applicationId) throws ValidationException {
        if (store.findApplication(applicationId) == null) {
            throw new ValidationException("This application no longer exists.");
        }
        store.deleteApplication(applicationId);
    }

    public List<Application> search(String query, ApplicationStatus status) {
        String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        Map<String, Student> students = studentMap();
        Map<String, Scholarship> scholarships = scholarshipMap();

        return all().stream()
                .filter(a -> status == null || a.status() == status)
                .filter(a -> {
                    if (needle.isEmpty()) {
                        return true;
                    }
                    Student s = students.get(a.studentId());
                    Scholarship sch = scholarships.get(a.scholarshipId());
                    return a.id().toLowerCase(Locale.ROOT).contains(needle)
                            || (s != null && s.fullName().toLowerCase(Locale.ROOT).contains(needle))
                            || (sch != null && sch.name().toLowerCase(Locale.ROOT).contains(needle));
                })
                .toList();
    }

    public Map<String, Student> studentMap() {
        Map<String, Student> map = new java.util.HashMap<>();
        for (Student student : store.students()) {
            map.put(student.id(), student);
        }
        return map;
    }

    public Map<String, Scholarship> scholarshipMap() {
        Map<String, Scholarship> map = new java.util.HashMap<>();
        for (Scholarship scholarship : store.scholarships()) {
            map.put(scholarship.id(), scholarship);
        }
        return map;
    }
}
