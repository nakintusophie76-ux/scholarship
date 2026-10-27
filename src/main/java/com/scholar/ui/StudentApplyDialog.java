package com.scholar.ui;

import com.scholar.model.Scholarship;
import com.scholar.model.Student;
import com.scholar.service.ApplicationService;
import com.scholar.service.ValidationException;
import com.scholar.util.Dates;
import com.scholar.util.Money;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

/**
 * Student-facing application form. The signed in student comes from the portal
 * session, so the form only asks for the scholarship statement.
 */
public final class StudentApplyDialog extends BaseDialog {

    private final ApplicationService service;
    private final Student student;
    private final Scholarship scholarship;
    private final JTextArea statementArea = new JTextArea(5, 30);
    private final JLabel eligibilityLabel = new JLabel();
    private final RoundedPanel eligibilityCard = new RoundedPanel();
    private final JButton submitButton;

    private boolean submitted;

    private StudentApplyDialog(Component parent, ApplicationService service,
                               Student student, Scholarship scholarship) {
        super(parent, "Apply for " + scholarship.name(),
                "Tell the scholarship office why this award matters to you.", 700);
        this.service = service;
        this.student = student;
        this.scholarship = scholarship;

        long approved = service.approvedCount(scholarship.id());
        int remaining = scholarship.remainingSlots(approved);

        JPanel info = new JPanel(new GridBagLayout());
        info.setOpaque(false);
        addInfo(info, 0, 0, "Sponsor", scholarship.sponsor());
        addInfo(info, 1, 0, "Award", Money.format(scholarship.amount()));
        addInfo(info, 0, 1, "Deadline", Dates.format(scholarship.deadline()));
        addInfo(info, 1, 1, "Places left", remaining + " of " + scholarship.slots());
        addInfo(info, 0, 2, "Minimum GPA", Money.gpa(scholarship.minGpa()));
        addInfo(info, 1, 2, "Income limit", scholarship.hasIncomeLimit()
                ? Money.format(scholarship.maxIncome()) : "No limit");

        JLabel description = new JLabel("<html><body style='width:560px'>"
                + UiUtils.escapeHtml(scholarship.description()) + "</body></html>");
        description.setFont(Theme.font(Font.PLAIN, 13));
        description.setForeground(Theme.MUTED);

        statementArea.setFont(Theme.font(Font.PLAIN, 14));
        statementArea.setLineWrap(true);
        statementArea.setWrapStyleWord(true);
        statementArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        statementArea.setBackground(Theme.CARD);
        statementArea.setForeground(Theme.TEXT);

        JScrollPane statementScroll = new JScrollPane(statementArea);
        statementScroll.setPreferredSize(new Dimension(0, 110));
        statementScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1, true));

        eligibilityCard.setLayout(new BorderLayout());
        eligibilityCard.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        eligibilityCard.shadow(false);
        eligibilityCard.borderColor(null);
        eligibilityLabel.setFont(Theme.font(Font.PLAIN, 12));
        eligibilityLabel.setVerticalAlignment(JLabel.TOP);
        eligibilityCard.add(eligibilityLabel, BorderLayout.CENTER);

        FormPanel form = new FormPanel();
        form.addFull(info);
        form.addFull(description);
        form.addFull(UiUtils.labeled("Personal statement (optional)", statementScroll));
        form.addFull(eligibilityCard);

        RoundedPanel card = new RoundedPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 6, 20));
        card.add(form, BorderLayout.CENTER);
        setBody(card);

        JButton cancel = UiUtils.secondaryButton("Cancel");
        cancel.addActionListener(e -> dispose());
        submitButton = UiUtils.primaryButton("Submit application");
        submitButton.addActionListener(e -> submit());
        setButtons(cancel, submitButton);
        getRootPane().setDefaultButton(submitButton);

        updateEligibility();
        finish();
    }

    private void updateEligibility() {
        List<String> problems = service.eligibilityProblems(student.id(), scholarship.id());
        if (problems.isEmpty()) {
            eligibilityLabel.setText("<html><body style='width:560px'><b>You are eligible to apply.</b><br>"
                    + "Your record meets the GPA, income, deadline and capacity requirements.</body></html>");
            eligibilityLabel.setForeground(Theme.SUCCESS);
            eligibilityCard.fill(Theme.SUCCESS_BG).repaint();
            submitButton.setEnabled(true);
        } else {
            StringBuilder html = new StringBuilder("<html><body style='width:560px'>"
                    + "<b>This application cannot be submitted:</b><br>");
            for (String problem : problems) {
                html.append("• ").append(UiUtils.escapeHtml(problem)).append("<br>");
            }
            html.append("</body></html>");
            eligibilityLabel.setText(html.toString());
            eligibilityLabel.setForeground(Theme.DANGER);
            eligibilityCard.fill(Theme.DANGER_BG).repaint();
            submitButton.setEnabled(false);
        }
    }

    private void submit() {
        try {
            var application = service.apply(student.id(), scholarship.id(),
                    statementArea.getText());
            submitted = true;
            UiUtils.info(this, "Application " + application.id() + " was submitted.\n"
                    + "You can track the office decision under My applications.");
            dispose();
        } catch (ValidationException ex) {
            UiUtils.error(this, ex.getMessage());
            updateEligibility();
        }
    }

    private static void addInfo(JPanel panel, int column, int row, String label, String value) {
        JPanel item = new JPanel();
        item.setOpaque(false);
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
        JLabel caption = UiUtils.label(label.toUpperCase(), Font.BOLD, 10, Theme.MUTED);
        JLabel content = UiUtils.label(value, Font.PLAIN, 13, Theme.TEXT);
        item.add(caption);
        item.add(Box.createVerticalStrut(2));
        item.add(content);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = column;
        gbc.gridy = row;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(0, 0, 12, 12);
        panel.add(item, gbc);
    }

    public static boolean showDialog(Component parent, ApplicationService service,
                                     Student student, Scholarship scholarship) {
        StudentApplyDialog dialog = new StudentApplyDialog(parent, service, student,
                scholarship);
        dialog.setVisible(true);
        return dialog.submitted;
    }
}
