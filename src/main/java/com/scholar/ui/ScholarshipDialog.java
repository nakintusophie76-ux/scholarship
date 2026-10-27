package com.scholar.ui;

import com.scholar.model.Scholarship;
import com.scholar.model.ScholarshipType;
import com.scholar.service.ScholarshipService;
import com.scholar.service.ValidationException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Create or edit a scholarship programme.
 */
public final class ScholarshipDialog extends BaseDialog {

    private final ScholarshipService service;
    private final Scholarship existing;

    private final JTextField nameField = UiUtils.textField("e.g. Future Innovators Merit Award");
    private final JTextField sponsorField = UiUtils.textField("e.g. Horizon Foundation");
    private final JComboBox<ScholarshipType> typeCombo = UiUtils.comboBox(ScholarshipType.values());
    private final JTextField amountField = UiUtils.textField("e.g. 5000");
    private final JTextField slotsField = UiUtils.textField("e.g. 5");
    private final JTextField minGpaField = UiUtils.textField("e.g. 3.50");
    private final JTextField maxIncomeField = UiUtils.textField("0 = no income limit");
    private final JTextField deadlineField = UiUtils.textField("yyyy-mm-dd");
    private final JCheckBox activeCheck = new JCheckBox("Accepting applications");
    private final JTextArea descriptionArea = new JTextArea(4, 30);

    private Scholarship saved;

    private ScholarshipDialog(Component parent, ScholarshipService service, Scholarship existing) {
        super(parent,
                existing == null ? "New scholarship" : "Edit scholarship",
                existing == null
                        ? "Define the award, eligibility thresholds and deadline."
                        : "Update the details of " + existing.name() + ".",
                720);
        this.service = service;
        this.existing = existing;

        descriptionArea.setFont(Theme.font(Font.PLAIN, 14));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        descriptionArea.setBackground(Theme.CARD);
        descriptionArea.setForeground(Theme.TEXT);

        activeCheck.setOpaque(false);
        activeCheck.setFont(Theme.font(Font.PLAIN, 13));
        activeCheck.setForeground(Theme.TEXT);
        activeCheck.setFocusPainted(false);

        if (existing != null) {
            nameField.setText(existing.name());
            sponsorField.setText(existing.sponsor());
            typeCombo.setSelectedItem(existing.type());
            amountField.setText(com.scholar.util.Money.plain(existing.amount()));
            slotsField.setText(Integer.toString(existing.slots()));
            minGpaField.setText(com.scholar.util.Money.gpa(existing.minGpa()));
            maxIncomeField.setText(com.scholar.util.Money.plain(existing.maxIncome()));
            deadlineField.setText(existing.deadline() == null ? "" : existing.deadline().toString());
            activeCheck.setSelected(existing.active());
            descriptionArea.setText(existing.description());
        } else {
            typeCombo.setSelectedItem(ScholarshipType.MERIT);
            slotsField.setText("1");
            minGpaField.setText("0.00");
            maxIncomeField.setText("0");
            deadlineField.setText(LocalDate.now().plusDays(30).toString());
            activeCheck.setSelected(true);
        }

        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        descriptionScroll.setPreferredSize(new Dimension(0, 96));
        descriptionScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1, true));

        FormPanel form = new FormPanel();
        form.addFull(UiUtils.labeled("Scholarship name", nameField));
        form.addRow(UiUtils.labeled("Sponsor", sponsorField),
                UiUtils.labeled("Type", typeCombo));
        form.addRow(UiUtils.labeled("Award amount (USD)", amountField),
                UiUtils.labeled("Number of places", slotsField));
        form.addRow(UiUtils.labeled("Minimum GPA", minGpaField),
                UiUtils.labeled("Maximum household income (0 = none)", maxIncomeField));
        form.addRow(UiUtils.labeled("Application deadline", deadlineField),
                UiUtils.labeled("Availability", activeCheck));
        form.addFull(UiUtils.labeled("Description", descriptionScroll));

        RoundedPanel card = new RoundedPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 6, 20));
        card.add(form, BorderLayout.CENTER);
        setBody(card);

        JButton cancel = UiUtils.secondaryButton("Cancel");
        cancel.addActionListener(e -> dispose());
        JButton save = UiUtils.primaryButton(existing == null ? "Create scholarship" : "Save changes");
        save.addActionListener(e -> save());
        setButtons(cancel, save);
        getRootPane().setDefaultButton(save);

        finish();
    }

    private void save() {
        try {
            double amount = UiUtils.parseDecimal(amountField.getText(), "Award amount");
            int slots = UiUtils.parseInteger(slotsField.getText(), "Number of places");
            double minGpa = UiUtils.parseDecimal(minGpaField.getText(), "Minimum GPA");
            double maxIncome = UiUtils.parseDecimal(maxIncomeField.getText(), "Maximum household income");
            String rawDeadline = deadlineField.getText() == null ? "" : deadlineField.getText().trim();
            LocalDate deadline;
            try {
                deadline = LocalDate.parse(rawDeadline);
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException(
                        "Deadline must use the format yyyy-mm-dd, for example 2026-12-31.");
            }

            Scholarship draft = new Scholarship(
                    existing == null ? "" : existing.id(),
                    nameField.getText(),
                    sponsorField.getText(),
                    (ScholarshipType) typeCombo.getSelectedItem(),
                    amount,
                    slots,
                    minGpa,
                    maxIncome,
                    deadline,
                    descriptionArea.getText(),
                    activeCheck.isSelected());
            saved = service.save(draft);
            dispose();
        } catch (IllegalArgumentException ex) {
            UiUtils.error(this, ex.getMessage());
        } catch (ValidationException ex) {
            UiUtils.error(this, ex.getMessage());
        }
    }

    public static boolean showDialog(Component parent, ScholarshipService service, Scholarship existing) {
        ScholarshipDialog dialog = new ScholarshipDialog(parent, service, existing);
        dialog.setVisible(true);
        return dialog.saved != null;
    }
}
