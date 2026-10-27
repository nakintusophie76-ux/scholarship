package com.scholar.ui;

import com.scholar.model.StudentAccount;
import com.scholar.model.StudentStatus;
import com.scholar.service.StudentAccountService;
import com.scholar.service.ValidationException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Self-service registration. New students can create their student record and
 * portal account in one step. Existing students can also link to their record.
 */
public final class StudentRegistrationDialog extends BaseDialog {

    private final StudentAccountService service;

    private final JTextField studentIdField = UiUtils.textField("Leave blank to create new student record");
    private final JTextField emailField = UiUtils.textField("student@example.edu");
    private final JTextField firstNameField = UiUtils.textField("First name");
    private final JTextField lastNameField = UiUtils.textField("Last name");
    private final JTextField phoneField = UiUtils.textField("+1 202 555 0100");
    private final JTextField programField = UiUtils.textField("e.g. Computer Science");
    private final JTextField yearField = UiUtils.textField("1-6");
    private final JTextField gpaField = UiUtils.textField("e.g. 3.50");
    private final JTextField incomeField = UiUtils.textField("0");
    private final JComboBox<StudentStatus> statusCombo = UiUtils.comboBox(StudentStatus.values());
    private final JTextField usernameField = UiUtils.textField("3–20 letters, numbers, dots, dashes");
    private final JPasswordField passwordField = UiUtils.passwordField("At least 6 characters");
    private final JPasswordField confirmField = UiUtils.passwordField("Repeat the password");

    private StudentAccount created;

    private StudentRegistrationDialog(Component parent, StudentAccountService service) {
        super(parent, "Create a student account",
                "Create a new student profile and portal account, or link to an existing record.",
                780);
        this.service = service;

        JLabel hint = new JLabel("<html><body style='width:640px'>"
                + "Leave Student ID blank to create a new student record with your details. "
                + "If you already have a student record, enter your Student ID and email on file. "
                + "Your account lets you browse scholarships, apply and track decisions."
                + "</body></html>");
        hint.setFont(Theme.font(Font.PLAIN, 12));
        hint.setForeground(Theme.MUTED);
        hint.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        FormPanel form = new FormPanel();
        form.addRow(UiUtils.labeled("Student ID (optional)", studentIdField),
                UiUtils.labeled("Email", emailField));
        form.addRow(UiUtils.labeled("First name", firstNameField),
                UiUtils.labeled("Last name", lastNameField));
        form.addRow(UiUtils.labeled("Phone", phoneField),
                UiUtils.labeled("Programme / course", programField));
        form.addRow(UiUtils.labeled("Year of study", yearField),
                UiUtils.labeled("GPA (0.00 - 4.00)", gpaField));
        form.addRow(UiUtils.labeled("Household income", incomeField),
                UiUtils.labeled("Enrollment status", statusCombo));
        form.addFull(UiUtils.labeled("Choose a username", usernameField));
        form.addRow(UiUtils.labeled("Password", passwordField),
                UiUtils.labeled("Confirm password", confirmField));
        form.addFull(hint);

        JPanel scrollContent = new JPanel(new BorderLayout());
        scrollContent.setOpaque(false);
        scrollContent.add(form, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(scrollContent);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setPreferredSize(new Dimension(0, 460));

        RoundedPanel card = new RoundedPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 8, 20));
        card.add(scroll, BorderLayout.CENTER);
        setBody(card);

        JButton cancel = UiUtils.secondaryButton("Cancel");
        cancel.addActionListener(e -> dispose());
        JButton create = UiUtils.primaryButton("Create account and save details");
        create.addActionListener(e -> register());
        setButtons(cancel, create);
        getRootPane().setDefaultButton(create);

        finish();
    }

    private void register() {
        try {
            Integer year = parseInt(yearField.getText());
            Double gpa = parseDouble(gpaField.getText());
            Double income = parseDouble(incomeField.getText());
            StudentStatus status = (StudentStatus) statusCombo.getSelectedItem();

            created = service.register(studentIdField.getText(), emailField.getText(),
                    usernameField.getText(), new String(passwordField.getPassword()),
                    new String(confirmField.getPassword()),
                    firstNameField.getText(), lastNameField.getText(), phoneField.getText(),
                    programField.getText(), year, gpa, income, status);
            UiUtils.info(this, "Your student account and details have been saved.\n"
                    + "You are now signed in to the student portal.");
            dispose();
        } catch (ValidationException ex) {
            UiUtils.error(this, ex.getMessage());
        }
    }

    private static Integer parseInt(String value) {
        String cleaned = value == null ? "" : value.trim();
        if (cleaned.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(cleaned);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Double parseDouble(String value) {
        String cleaned = value == null ? "" : value.trim();
        if (cleaned.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static StudentAccount showDialog(Component parent, StudentAccountService service) {
        StudentRegistrationDialog dialog = new StudentRegistrationDialog(parent, service);
        dialog.setVisible(true);
        return dialog.created;
    }
}
