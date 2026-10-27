package com.scholar.service;

import com.scholar.model.AdminUser;
import com.scholar.store.DataStore;
import com.scholar.util.Passwords;

/**
 * Sign in and password management for scholarship office staff.
 */
public final class AuthService {

    private final DataStore store;

    public AuthService(DataStore store) {
        this.store = store;
    }

    public AdminUser login(String username, String password) throws ValidationException {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            throw new ValidationException("Enter your username and password.");
        }
        AdminUser user = store.findUser(username);
        if (user == null || !Passwords.matches(password, user.salt(), user.passwordHash())) {
            throw new ValidationException("Invalid username or password.");
        }
        return user;
    }

    public AdminUser changePassword(AdminUser user, String currentPassword,
                                    String newPassword, String confirmPassword)
            throws ValidationException {

        if (user == null) {
            throw new ValidationException("You are not signed in.");
        }
        if (!Passwords.matches(currentPassword, user.salt(), user.passwordHash())) {
            throw new ValidationException("The current password is incorrect.");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new ValidationException("The new password must have at least 6 characters.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new ValidationException("The new passwords do not match.");
        }
        if (newPassword.equals(currentPassword)) {
            throw new ValidationException("Choose a password different from the current one.");
        }

        String salt = Passwords.newSalt();
        AdminUser updated = new AdminUser(user.username(), user.displayName(), salt,
                Passwords.hash(newPassword, salt));
        store.saveUser(updated);
        return updated;
    }
}
