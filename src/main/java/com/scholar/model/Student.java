package com.scholar.model;

/**
 * A student who can apply for scholarships.
 */
public record Student(
        String id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String program,
        int year,
        double gpa,
        double householdIncome,
        StudentStatus status) {

    public String fullName() {
        return (firstName + " " + lastName).trim();
    }

    public String initials() {
        String first = firstName == null || firstName.isBlank() ? "" : firstName.substring(0, 1);
        String last = lastName == null || lastName.isBlank() ? "" : lastName.substring(0, 1);
        String result = (first + last).toUpperCase();
        return result.isBlank() ? "?" : result;
    }
}
