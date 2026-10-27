package com.scholar.model;

/**
 * Lifecycle state of a student in the scholarship office records.
 */
public enum StudentStatus {

    ACTIVE("Active"),
    INACTIVE("Inactive"),
    GRADUATED("Graduated");

    private final String label;

    StudentStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
