package com.scholar.ui;

import com.scholar.model.Student;
import com.scholar.model.StudentStatus;
import com.scholar.service.StudentService;
import com.scholar.service.ValidationException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;

/**
 * Create or edit a student record.
 */
public final class StudentDialog extends BaseDialog {

    private static final Integer[] YEARS = {1, 2, 3, 4, 5, 6};

    private final StudentService service;
    private final Student existing;

    private final JTextField firstNameField = UiUtils.textField("e.g. Amina");
    private final JTextField lastNameField = UiUtils.textField("e.g. Yusuf");
    private final JTextField emailField = UiUtils.textField("student@example.edu");
    private final JTextField phoneField = UiUtils.textField("+1 202 555 0100");
    private final JTextField programField = UiUtils.textField("e.g. Computer Science");
    private final JComboBox<Integer> yearCombo = UiUtils.comboBox(YEARS);
    private final JTextField gpaField = UiUtils.textField("e.g. 3.75");
    private final JTextField incomeField = UiUtils.textField("Annual household income in USD");
    private final JComboBox<StudentStatus> statusCombo = UiUtils.comboBox(StudentStatus.values());

    private Student saved;

    private StudentDialog(Component parent, StudentService service, Student existing) {
        super(parent,
                existing == null ? "Add student" : "Edit student",
                existing == null
                        ? "Create a record so the student can apply for scholarships."
                        : "Update the academic and household details for " + existing.fullName() + ".",
                660);
        this.service = service;
        this.existing = existing;

        if (existing != null) {
            firstNameField.setText(existing.firstName());
            lastNameField.setText(existing.lastName());
            emailField.setText(existing.email());
            phoneField.setText(existing.phone());
            programField.setText(existing.program());
            yearCombo.setSelectedItem(Math.max(1, Math.min(6, existing.year())));
            gpaField.setText(com.scholar.util.Money.gpa(existing.gpa()));
            incomeField.setText(com.scholar.util.Money.plain(existing.householdIncome()));
            statusCombo.setSelectedItem(existing.status());
        } else {
            yearCombo.setSelectedItem(1);
            statusCombo.setSelectedItem(StudentStatus.ACTIVE);
            incomeField.setText("0");
        }

        FormPanel form = new FormPanel();
        form.addRow(UiUtils.labeled("First name", firstNameField),
                UiUtils.labeled("Last name", lastNameField));
        form.addFull(UiUtils.labeled("Email address", emailField));
        form.addRow(UiUtils.labeled("Phone", phoneField),
                UiUtils.labeled("Programme / course", programField));
        form.addRow(UiUtils.labeled("Year of study", yearCombo),
                UiUtils.labeled("GPA (0.00 – 4.00)", gpaField));
        form.addRow(UiUtils.labeled("Household income (USD / year)", incomeField),
                UiUtils.labeled("Enrollment status", statusCombo));

        RoundedPanel card = new RoundedPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 8, 20));
        card.add(form, BorderLayout.CENTER);
        setBody(card);

        JButton cancel = UiUtils.secondaryButton("Cancel");
        cancel.addActionListener(e -> dispose());
        JButton save = UiUtils.primaryButton(existing == null ? "Add student" : "Save changes");
        save.addActionListener(e -> save());
        setButtons(cancel, save);
        getRootPane().setDefaultButton(save);

        finish();
    }

    private void save() {
        try {
            double gpa = UiUtils.parseDecimal(gpaField.getText(), "GPA");
            double income = UiUtils.parseDecimal(incomeField.getText(), "Household income");
            Student draft = new Student(
                    existing == null ? "" : existing.id(),
                    firstNameField.getText(),
                    lastNameField.getText(),
                    emailField.getText(),
                    phoneField.getText(),
                    programField.getText(),
                    (Integer) yearCombo.getSelectedItem(),
                    gpa,
                    income,
                    (StudentStatus) statusCombo.getSelectedItem());
            saved = service.save(draft);
            dispose();
        } catch (IllegalArgumentException ex) {
            UiUtils.error(this, ex.getMessage());
        } catch (ValidationException ex) {
            UiUtils.error(this, ex.getMessage());
        }
    }

    public static boolean showDialog(Component parent, StudentService service, Student existing) {
        StudentDialog dialog = new StudentDialog(parent, service, existing);
        dialog.setVisible(true);
        return dialog.saved != null;
    }
}
