package com.scholar.ui;

import com.scholar.model.Student;
import com.scholar.model.StudentAccount;
import com.scholar.service.StudentService;
import com.scholar.service.ValidationException;
import com.scholar.util.Money;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

/**
 * Student account page: contact details can be maintained here, while the
 * academic and household parts of the record stay under office control.
 */
public final class StudentProfilePanel extends JPanel implements Refreshable {

    private final StudentService students;
    private final String studentId;
    private final StudentAccount account;

    private final JLabel initialsLabel = UiUtils.label("?", Font.BOLD, 16, Color.WHITE);
    private final JLabel nameLabel = UiUtils.label("", Font.BOLD, 18, Theme.INK);
    private final JLabel idLabel = UiUtils.muted("");
    private final JLabel usernameLabel = UiUtils.muted("");
    private final JLabel statusPill = UiUtils.label("", Font.BOLD, 11, Theme.SUCCESS);
    private RoundedPanel statusPillPanel;

    private final JTextField emailField = UiUtils.textField("student@example.edu");
    private final JTextField phoneField = UiUtils.textField("+1 202 555 0100");

    private final JLabel programmeValue = valueLabel();
    private final JLabel yearValue = valueLabel();
    private final JLabel gpaValue = valueLabel();
    private final JLabel incomeValue = valueLabel();
    private final JLabel recordStatusValue = valueLabel();
    private final JLabel recordIdValue = valueLabel();

    public StudentProfilePanel(StudentService students, StudentAccount account) {
        this.students = students;
        this.studentId = account.studentId();
        this.account = account;

        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        add(buildBanner(), BorderLayout.NORTH);
        add(buildColumns(), BorderLayout.CENTER);

        refresh();
    }

    private RoundedPanel buildBanner() {
        RoundedPanel banner = new RoundedPanel();
        banner.setLayout(new BorderLayout(16, 0));
        banner.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        banner.setPreferredSize(new Dimension(10, 92));

        RoundedPanel avatar = new RoundedPanel()
                .fill(Theme.PRIMARY)
                .borderColor(null)
                .shadow(false)
                .arc(26);
        avatar.setLayout(new GridBagLayout());
        avatar.setPreferredSize(new Dimension(52, 52));
        avatar.add(initialsLabel);
        banner.add(avatar, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        nameLabel.setAlignmentX(LEFT_ALIGNMENT);
        idLabel.setAlignmentX(LEFT_ALIGNMENT);
        usernameLabel.setAlignmentX(LEFT_ALIGNMENT);
        text.add(nameLabel);
        text.add(Box.createVerticalStrut(2));
        text.add(idLabel);
        text.add(Box.createVerticalStrut(2));
        text.add(usernameLabel);
        banner.add(text, BorderLayout.CENTER);

        RoundedPanel pill = new RoundedPanel()
                .fill(Theme.SUCCESS_BG)
                .borderColor(null)
                .shadow(false)
                .arc(30);
        pill.setLayout(new GridBagLayout());
        pill.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        pill.add(statusPill);
        statusPillPanel = pill;
        JPanel pillWrap = new JPanel(new GridBagLayout());
        pillWrap.setOpaque(false);
        pillWrap.add(pill);
        banner.add(pillWrap, BorderLayout.EAST);

        return banner;
    }

    private JPanel buildColumns() {
        JPanel columns = new JPanel(new GridLayout(1, 2, 16, 0));
        columns.setOpaque(false);
        columns.add(buildContactCard());
        columns.add(buildAcademicCard());
        return columns;
    }

    private RoundedPanel buildContactCard() {
        RoundedPanel card = new RoundedPanel();
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        card.add(header("Contact details",
                "Keep your email and phone number up to date."), BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        JPanel emailRow = UiUtils.labeled("Email address", emailField);
        emailRow.setAlignmentX(LEFT_ALIGNMENT);
        emailRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        JPanel phoneRow = UiUtils.labeled("Phone", phoneField);
        phoneRow.setAlignmentX(LEFT_ALIGNMENT);
        phoneRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        JButton save = UiUtils.primaryButton("Save contact details");
        save.setAlignmentX(LEFT_ALIGNMENT);
        save.addActionListener(e -> saveContact());

        JLabel note = new JLabel("<html><body style='width:280px'>"
                + "Updating your email does not change your portal username or password."
                + "</body></html>");
        note.setFont(Theme.font(Font.PLAIN, 11));
        note.setForeground(Theme.MUTED);
        note.setAlignmentX(LEFT_ALIGNMENT);

        form.add(emailRow);
        form.add(Box.createVerticalStrut(10));
        form.add(phoneRow);
        form.add(Box.createVerticalStrut(14));
        form.add(save);
        form.add(Box.createVerticalStrut(14));
        form.add(note);
        card.add(form, BorderLayout.CENTER);

        return card;
    }

    private RoundedPanel buildAcademicCard() {
        RoundedPanel card = new RoundedPanel();
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        card.add(header("Academic record",
                "Managed by the scholarship office."), BorderLayout.NORTH);

        JPanel rows = new JPanel(new GridBagLayout());
        rows.setOpaque(false);
        int row = 0;
        addRow(rows, row++, "Student ID", recordIdValue);
        addRow(rows, row++, "Programme", programmeValue);
        addRow(rows, row++, "Year of study", yearValue);
        addRow(rows, row++, "GPA", gpaValue);
        addRow(rows, row++, "Household income", incomeValue);
        addRow(rows, row, "Enrollment status", recordStatusValue);
        card.add(rows, BorderLayout.CENTER);

        JLabel note = new JLabel("<html><body style='width:280px'>"
                + "Need to change your programme, GPA or household income? "
                + "Contact the scholarship office with supporting documents."
                + "</body></html>");
        note.setFont(Theme.font(Font.PLAIN, 11));
        note.setForeground(Theme.MUTED);
        note.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        card.add(note, BorderLayout.SOUTH);

        return card;
    }

    private static JPanel header(String title, String subtitle) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel titleLabel = UiUtils.label(title, Font.BOLD, 15, Theme.TEXT);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        JLabel subtitleLabel = UiUtils.label(subtitle, Font.PLAIN, 12, Theme.MUTED);
        subtitleLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(2));
        panel.add(subtitleLabel);
        return panel;
    }

    private static JLabel valueLabel() {
        return UiUtils.label("", Font.BOLD, 13, Theme.TEXT);
    }

    private static void addRow(JPanel panel, int row, String label, JLabel value) {
        JLabel caption = UiUtils.label(label, Font.PLAIN, 12, Theme.MUTED);
        GridBagConstraints left = new GridBagConstraints();
        left.gridx = 0;
        left.gridy = row;
        left.anchor = GridBagConstraints.WEST;
        left.insets = new Insets(5, 0, 5, 16);
        panel.add(caption, left);

        GridBagConstraints right = new GridBagConstraints();
        right.gridx = 1;
        right.gridy = row;
        right.weightx = 1;
        right.anchor = GridBagConstraints.EAST;
        right.insets = new Insets(5, 0, 5, 0);
        panel.add(value, right);
    }

    private void saveContact() {
        try {
            students.updateContact(studentId, emailField.getText(), phoneField.getText());
            refresh();
            UiUtils.info(this, "Your contact details have been updated.");
        } catch (ValidationException ex) {
            UiUtils.error(this, ex.getMessage());
        }
    }

    @Override
    public void refresh() {
        Student student = students.find(studentId);
        if (student == null) {
            return;
        }
        initialsLabel.setText(student.initials());
        nameLabel.setText(student.fullName());
        idLabel.setText(student.id() + "  •  " + student.program());
        usernameLabel.setText("Portal username: " + account.username());
        statusPill.setText(student.status().label());
        Color foreground = UiUtils.badgeForeground(student.status());
        statusPill.setForeground(foreground);
        Color background = switch (student.status()) {
            case ACTIVE -> Theme.SUCCESS_BG;
            case INACTIVE -> Theme.DANGER_BG;
            case GRADUATED -> Theme.NEUTRAL_BG;
        };
        statusPillPanel.fill(background).repaint();

        emailField.setText(student.email());
        phoneField.setText(student.phone());

        recordIdValue.setText(student.id());
        programmeValue.setText(student.program());
        yearValue.setText("Year " + student.year());
        gpaValue.setText(Money.gpa(student.gpa()) + " / 4.00");
        incomeValue.setText(Money.format(student.householdIncome()) + " / year");
        recordStatusValue.setText(student.status().label());
    }
}
