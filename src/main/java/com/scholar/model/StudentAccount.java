package com.scholar.model;

/**
 * A student's self-service portal account. It is linked to an existing student
 * record and stores only a salt and a PBKDF2 password hash.
 */
public record StudentAccount(
        String studentId,
        String username,
        String salt,
        String passwordHash) {

    public StudentAccount withPassword(String newSalt, String newPasswordHash) {
        return new StudentAccount(studentId, username, newSalt, newPasswordHash);
    }
}
