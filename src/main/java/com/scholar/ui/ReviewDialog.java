package com.scholar.ui;

import com.scholar.model.Application;
import com.scholar.model.ApplicationStatus;
import com.scholar.model.Scholarship;
import com.scholar.model.Student;
import com.scholar.service.ApplicationService;
import com.scholar.service.ValidationException;
import com.scholar.util.Dates;
import com.scholar.util.Money;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
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

/**
 * Review an application and record the award decision.
 */
public final class ReviewDialog extends BaseDialog {

    private final ApplicationService service;
    private final Application application;
    private final JComboBox<ApplicationStatus> statusCombo =
            UiUtils.comboBox(ApplicationStatus.values());
    private final JTextArea notesArea = new JTextArea(4, 30);

    private Application updated;

    private ReviewDialog(Component parent, ApplicationService service, Application application) {
        super(parent, "Award decision for " + application.id(),
                "Check the statement, then award the scholarship or record the office decision.", 720);
        this.service = service;
        this.application = application;

        Student student = service.studentMap().get(application.studentId());
        Scholarship scholarship = service.scholarshipMap().get(application.scholarshipId());
        long approved = scholarship == null ? 0 : service.approvedCount(scholarship.id());

        JPanel info = new JPanel(new GridBagLayout());
        info.setOpaque(false);
        addInfo(info, 0, 0, "Student", student == null ? application.studentId()
                : student.fullName() + " (" + student.id() + ")");
        addInfo(info, 1, 0, "Scholarship", scholarship == null ? application.scholarshipId()
                : scholarship.name());
        addInfo(info, 0, 1, "Award", scholarship == null ? "—" : Money.format(scholarship.amount()));
        addInfo(info, 1, 1, "Places awarded", scholarship == null ? "—"
                : approved + " of " + scholarship.slots());
        addInfo(info, 0, 2, "Submitted", Dates.format(application.submittedAt()));
        addInfo(info, 1, 2, "Current status", application.status().label());

        notesArea.setFont(Theme.font(Font.PLAIN, 14));
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        notesArea.setBackground(Theme.CARD);
        notesArea.setForeground(Theme.TEXT);
        notesArea.setText(application.reviewNotes());

        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setPreferredSize(new Dimension(0, 96));
        notesScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1, true));

        JTextArea statementArea = new JTextArea(application.statement());
        statementArea.setEditable(false);
        statementArea.setLineWrap(true);
        statementArea.setWrapStyleWord(true);
        statementArea.setFont(Theme.font(Font.PLAIN, 13));
        statementArea.setForeground(Theme.TEXT);
        statementArea.setBackground(Theme.ROW_ALT);
        statementArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        JScrollPane statementScroll = new JScrollPane(statementArea);
        statementScroll.setPreferredSize(new Dimension(0, 78));
        statementScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1, true));

        statusCombo.setSelectedItem(application.status());

        FormPanel form = new FormPanel();
        form.addFull(info);
        form.addFull(UiUtils.labeled("Applicant statement", statementScroll));
        form.addRow(UiUtils.labeled("Award decision", statusCombo));
        form.addFull(UiUtils.labeled("Review notes", notesScroll));

        if (scholarship != null && approved >= scholarship.slots()
                && application.status() != ApplicationStatus.APPROVED) {
            form.addFull(warning("All places for this scholarship are already awarded. "
                    + "Awarding another application will be blocked until another awardee is waitlisted."));
        }

        RoundedPanel card = new RoundedPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 6, 20));
        card.add(form, BorderLayout.CENTER);
        setBody(card);

        JButton cancel = UiUtils.secondaryButton("Cancel");
        cancel.addActionListener(e -> dispose());
        JButton save = UiUtils.primaryButton("Save award decision");
        save.addActionListener(e -> save());
        setButtons(cancel, save);
        getRootPane().setDefaultButton(save);

        finish();
    }

    private void save() {
        try {
            updated = service.review(application.id(),
                    (ApplicationStatus) statusCombo.getSelectedItem(), notesArea.getText());
            dispose();
        } catch (ValidationException ex) {
            UiUtils.error(this, ex.getMessage());
        }
    }

    private static JLabel warning(String text) {
        JLabel label = new JLabel("<html><body style='width:560px'>" + text + "</body></html>");
        label.setFont(Theme.font(Font.PLAIN, 12));
        label.setForeground(Theme.WARNING);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        return label;
    }

    private static void addInfo(JPanel panel, int column, int row, String label, String value) {
        JPanel item = new JPanel();
        item.setOpaque(false);
        item.setLayout(new javax.swing.BoxLayout(item, javax.swing.BoxLayout.Y_AXIS));
        JLabel caption = UiUtils.label(label.toUpperCase(), Font.BOLD, 10, Theme.MUTED);
        JLabel content = UiUtils.label(value, Font.PLAIN, 13, Theme.TEXT);
        item.add(caption);
        item.add(javax.swing.Box.createVerticalStrut(2));
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
                                     Application application) {
        ReviewDialog dialog = new ReviewDialog(parent, service, application);
        dialog.setVisible(true);
        return dialog.updated != null;
    }
}
