package com.scholar.model;

/**
 * Review workflow states for an application.
 */
public enum ApplicationStatus {

    PENDING("Pending"),
    APPROVED("Approved"),
    WAITLISTED("Waitlisted"),
    REJECTED("Rejected");

    private final String label;

    ApplicationStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public boolean isFinalDecision() {
        return this == APPROVED || this == REJECTED;
    }

    @Override
    public String toString() {
        return label;
    }
}
