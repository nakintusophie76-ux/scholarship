package com.scholar.model;

import java.time.LocalDateTime;

/**
 * A student's application to a scholarship, including its review outcome.
 */
public record Application(
        String id,
        String studentId,
        String scholarshipId,
        LocalDateTime submittedAt,
        ApplicationStatus status,
        String statement,
        String reviewNotes,
        LocalDateTime reviewedAt) {

    public Application withStatus(ApplicationStatus newStatus, String notes, LocalDateTime reviewed) {
        return new Application(id, studentId, scholarshipId, submittedAt, newStatus,
                statement, notes == null ? "" : notes, reviewed);
    }
}
