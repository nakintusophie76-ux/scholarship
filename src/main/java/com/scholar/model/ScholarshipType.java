package com.scholar.model;

/**
 * Categories of scholarship supported by the office.
 */
public enum ScholarshipType {

    MERIT("Merit"),
    NEED("Need"),
    SPORTS("Sports"),
    RESEARCH("Research"),
    COMMUNITY("Community");

    private final String label;

    ScholarshipType(String label) {
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
