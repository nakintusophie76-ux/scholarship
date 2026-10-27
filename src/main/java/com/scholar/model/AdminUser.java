package com.scholar.model;

/**
 * An office account allowed to sign in to the system.
 * The password is never stored in clear text: only a salt and a PBKDF2 hash.
 */
public record AdminUser(
        String username,
        String displayName,
        String salt,
        String passwordHash) {

    public String initials() {
        String[] parts = displayName == null ? new String[0] : displayName.trim().split("\\s+");
        if (parts.length == 0 || parts[0].isBlank()) {
            return "?";
        }
        String result = parts[0].substring(0, 1);
        if (parts.length > 1 && !parts[parts.length - 1].isBlank()) {
            result += parts[parts.length - 1].substring(0, 1);
        }
        return result.toUpperCase();
    }
}
