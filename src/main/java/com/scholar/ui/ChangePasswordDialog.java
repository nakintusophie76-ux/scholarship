package com.scholar.ui;

import com.scholar.model.AdminUser;
import com.scholar.service.AuthService;
import com.scholar.service.ValidationException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPasswordField;
import java.awt.BorderLayout;
import java.awt.Component;

/**
 * Change the password of the signed in office account.
 */
public final class ChangePasswordDialog extends BaseDialog {

    private final AuthService authService;
    private final AdminUser user;

    private final JPasswordField currentField = UiUtils.passwordField("Current password");
    private final JPasswordField newField = UiUtils.passwordField("At least 6 characters");
    private final JPasswordField confirmField = UiUtils.passwordField("Repeat the new password");

    private AdminUser updated;

    private ChangePasswordDialog(Component parent, AuthService authService, AdminUser user) {
        super(parent, "Change password", "Signed in as " + user.displayName()
                + " (" + user.username() + ").", 540);
        this.authService = authService;
        this.user = user;

        FormPanel form = new FormPanel();
        form.addFull(UiUtils.labeled("Current password", currentField));
        form.addFull(UiUtils.labeled("New password", newField));
        form.addFull(UiUtils.labeled("Confirm new password", confirmField));

        RoundedPanel card = new RoundedPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 8, 20));
        card.add(form, BorderLayout.CENTER);
        setBody(card);

        JButton cancel = UiUtils.secondaryButton("Cancel");
        cancel.addActionListener(e -> dispose());
        JButton save = UiUtils.primaryButton("Update password");
        save.addActionListener(e -> save());
        setButtons(cancel, save);
        getRootPane().setDefaultButton(save);

        finish();
    }

    private void save() {
        try {
            updated = authService.changePassword(user,
                    new String(currentField.getPassword()),
                    new String(newField.getPassword()),
                    new String(confirmField.getPassword()));
            UiUtils.info(this, "Your password has been updated.");
            dispose();
        } catch (ValidationException ex) {
            UiUtils.error(this, ex.getMessage());
        }
    }

    public static AdminUser showDialog(Component parent, AuthService authService, AdminUser user) {
        ChangePasswordDialog dialog = new ChangePasswordDialog(parent, authService, user);
        dialog.setVisible(true);
        return dialog.updated;
    }
}
