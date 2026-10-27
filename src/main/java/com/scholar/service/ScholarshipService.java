package com.scholar.service;

import com.scholar.model.Application;
import com.scholar.model.ApplicationStatus;
import com.scholar.model.Scholarship;
import com.scholar.store.DataStore;
import com.scholar.util.Ids;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Scholarship programmes and their capacity rules.
 */
public final class ScholarshipService {

    private final DataStore store;

    public ScholarshipService(DataStore store) {
        this.store = store;
    }

    public List<Scholarship> all() {
        return store.scholarships().stream()
                .sorted(Comparator.comparing(Scholarship::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public Scholarship find(String id) {
        return store.findScholarship(id);
    }

    public List<Scholarship> open() {
        LocalDate today = LocalDate.now();
        return all().stream()
                .filter(s -> s.isOpen(today))
                .sorted(Comparator.comparing(Scholarship::deadline))
                .toList();
    }

    public long approvedCount(String scholarshipId) {
        return store.applications().stream()
                .filter(a -> a.scholarshipId().equals(scholarshipId))
                .filter(a -> a.status() == ApplicationStatus.APPROVED)
                .count();
    }

    public int remainingSlots(Scholarship scholarship) {
        return scholarship.remainingSlots(approvedCount(scholarship.id()));
    }

    public String nextId() {
        return Ids.next("SCH", store.scholarships().stream().map(Scholarship::id).toList());
    }

    public Scholarship save(Scholarship input) throws ValidationException {
        String name = clean(input.name());
        String sponsor = clean(input.sponsor());
        String description = clean(input.description());

        if (name.isEmpty()) {
            throw new ValidationException("Scholarship name is required.");
        }
        if (sponsor.isEmpty()) {
            throw new ValidationException("Sponsor is required.");
        }
        if (input.type() == null) {
            throw new ValidationException("Choose a scholarship type.");
        }
        if (input.amount() <= 0) {
            throw new ValidationException("Award amount must be greater than zero.");
        }
        if (input.slots() < 1) {
            throw new ValidationException("There must be at least one available place.");
        }
        if (input.minGpa() < 0 || input.minGpa() > 4.0) {
            throw new ValidationException("Minimum GPA must be between 0.00 and 4.00.");
        }
        if (input.maxIncome() < 0) {
            throw new ValidationException("Income limit cannot be negative (use 0 for no limit).");
        }
        if (input.deadline() == null) {
            throw new ValidationException("Enter a deadline in the format yyyy-mm-dd.");
        }

        String id = clean(input.id());
        if (id.isEmpty()) {
            id = nextId();
        }
        final String resolvedId = id;

        boolean duplicateName = store.scholarships().stream()
                .anyMatch(s -> !s.id().equals(resolvedId) && s.name().equalsIgnoreCase(name));
        if (duplicateName) {
            throw new ValidationException("A scholarship called \"" + name + "\" already exists.");
        }

        Scholarship scholarship = new Scholarship(resolvedId, name, sponsor, input.type(),
                round2(input.amount()), input.slots(), round2(input.minGpa()),
                round2(input.maxIncome()), input.deadline(), description, input.active());
        store.saveScholarship(scholarship);
        return scholarship;
    }

    public void delete(String scholarshipId) throws ValidationException {
        Scholarship scholarship = store.findScholarship(scholarshipId);
        if (scholarship == null) {
            throw new ValidationException("This scholarship no longer exists.");
        }
        long applications = store.applications().stream()
                .filter(a -> a.scholarshipId().equals(scholarshipId))
                .count();
        if (applications > 0) {
            throw new ValidationException("Cannot delete \"" + scholarship.name() + "\" because "
                    + applications + " application(s) reference it.\n"
                    + "Delete those applications first or mark the scholarship inactive.");
        }
        store.deleteScholarship(scholarshipId);
    }

    public List<Scholarship> search(String query, Boolean openOnly) {
        String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        LocalDate today = LocalDate.now();
        return all().stream()
                .filter(s -> openOnly == null || s.isOpen(today) == openOnly)
                .filter(s -> needle.isEmpty()
                        || s.name().toLowerCase(Locale.ROOT).contains(needle)
                        || s.sponsor().toLowerCase(Locale.ROOT).contains(needle)
                        || s.type().label().toLowerCase(Locale.ROOT).contains(needle)
                        || s.id().toLowerCase(Locale.ROOT).contains(needle))
                .toList();
    }

    public double totalAwarded() {
        return store.applications().stream()
                .filter(a -> a.status() == ApplicationStatus.APPROVED)
                .mapToDouble(a -> {
                    Scholarship s = store.findScholarship(a.scholarshipId());
                    return s == null ? 0 : s.amount();
                })
                .sum();
    }

    public long countOpen() {
        LocalDate today = LocalDate.now();
        return store.scholarships().stream().filter(s -> s.isOpen(today)).count();
    }

    public List<Application> applicationsFor(String scholarshipId) {
        return store.applications().stream()
                .filter(a -> a.scholarshipId().equals(scholarshipId))
                .toList();
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
