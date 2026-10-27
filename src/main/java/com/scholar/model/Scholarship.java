package com.scholar.model;

import java.time.LocalDate;

/**
 * A scholarship programme offered by a sponsor.
 *
 * <p>{@code maxIncome} of 0 means "no household income limit".</p>
 */
public record Scholarship(
        String id,
        String name,
        String sponsor,
        ScholarshipType type,
        double amount,
        int slots,
        double minGpa,
        double maxIncome,
        LocalDate deadline,
        String description,
        boolean active) {

    public boolean isOpen(LocalDate today) {
        return active && deadline != null && !deadline.isBefore(today);
    }

    public boolean hasIncomeLimit() {
        return maxIncome > 0;
    }

    public int remainingSlots(long approvedApplications) {
        return Math.max(0, slots - (int) approvedApplications);
    }
}
