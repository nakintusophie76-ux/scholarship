package com.scholar.ui;

import com.scholar.model.StudentAccount;
import com.scholar.service.StudentAccountService;
import com.scholar.service.ValidationException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPasswordField;
import java.awt.BorderLayout;
import java.awt.Component;

/**
 * Change the password of the signed in student portal account.
 */
public final class StudentChangePasswordDialog extends BaseDialog {

    private final StudentAccountService service;
    private final StudentAccount account;

    private final JPasswordField currentField = UiUtils.passwordField("Current password");
    private final JPasswordField newField = UiUtils.passwordField("At least 6 characters");
    private final JPasswordField confirmField = UiUtils.passwordField("Repeat the new password");

    private StudentAccount updated;

    private StudentChangePasswordDialog(Component parent, StudentAccountService service,
                                        StudentAccount account) {
        super(parent, "Change password", "Signed in as " + account.username()
                + ". Use a password you do not use elsewhere.", 540);
        this.service = service;
        this.account = account;

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
            updated = service.changePassword(account,
                    new String(currentField.getPassword()),
                    new String(newField.getPassword()),
                    new String(confirmField.getPassword()));
            UiUtils.info(this, "Your password has been updated.");
            dispose();
        } catch (ValidationException ex) {
            UiUtils.error(this, ex.getMessage());
        }
    }

    public static StudentAccount showDialog(Component parent, StudentAccountService service,
                                            StudentAccount account) {
        StudentChangePasswordDialog dialog = new StudentChangePasswordDialog(parent, service,
                account);
        dialog.setVisible(true);
        return dialog.updated;
    }
}
