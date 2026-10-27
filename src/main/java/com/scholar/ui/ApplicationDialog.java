package com.scholar.ui;

import com.scholar.model.Application;
import com.scholar.model.Scholarship;
import com.scholar.model.Student;
import com.scholar.service.ApplicationService;
import com.scholar.service.ScholarshipService;
import com.scholar.service.StudentService;
import com.scholar.service.ValidationException;
import com.scholar.util.Money;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

/**
 * Submit a new application. Eligibility is calculated live, and the submit
 * button stays disabled until every rule is satisfied.
 */
public final class ApplicationDialog extends BaseDialog {

    private final ApplicationService service;
    private final JComboBox<Student> studentCombo;
    private final JComboBox<Scholarship> scholarshipCombo;
    private final JTextArea statementArea = new JTextArea(4, 30);
    private final JLabel eligibilityLabel = new JLabel();
    private final RoundedPanel eligibilityCard = new RoundedPanel();
    private final JButton submitButton;

    private boolean submitted;

    private ApplicationDialog(Component parent, ApplicationService service,
                              List<Student> students, List<Scholarship> scholarships) {
        super(parent, "New application", "Only students who meet every programme rule can apply.", 700);
        this.service = service;

        studentCombo = new JComboBox<>(students.toArray(new Student[0]));
        studentCombo.setFont(Theme.font(Font.PLAIN, 14));
        studentCombo.setPreferredSize(new Dimension(200, 38));
        studentCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof Student student) {
                    setText(student.fullName() + "  •  " + student.id()
                            + "  •  GPA " + Money.gpa(student.gpa()));
                }
                return this;
            }
        });

        scholarshipCombo = new JComboBox<>(scholarships.toArray(new Scholarship[0]));
        scholarshipCombo.setFont(Theme.font(Font.PLAIN, 14));
        scholarshipCombo.setPreferredSize(new Dimension(200, 38));
        scholarshipCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof Scholarship scholarship) {
                    setText(scholarship.name() + "  •  " + Money.format(scholarship.amount()));
                }
                return this;
            }
        });

        statementArea.setFont(Theme.font(Font.PLAIN, 14));
        statementArea.setLineWrap(true);
        statementArea.setWrapStyleWord(true);
        statementArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        statementArea.setBackground(Theme.CARD);
        statementArea.setForeground(Theme.TEXT);

        JScrollPane statementScroll = new JScrollPane(statementArea);
        statementScroll.setPreferredSize(new Dimension(0, 100));
        statementScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1, true));

        eligibilityCard.setLayout(new BorderLayout());
        eligibilityCard.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        eligibilityCard.shadow(false);
        eligibilityCard.borderColor(null);
        eligibilityLabel.setFont(Theme.font(Font.PLAIN, 12));
        eligibilityLabel.setVerticalAlignment(JLabel.TOP);
        eligibilityCard.add(eligibilityLabel, BorderLayout.CENTER);

        FormPanel form = new FormPanel();
        form.addFull(UiUtils.labeled("Student", studentCombo));
        form.addFull(UiUtils.labeled("Scholarship", scholarshipCombo));
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

        studentCombo.addActionListener(e -> updateEligibility());
        scholarshipCombo.addActionListener(e -> updateEligibility());
        updateEligibility();

        finish();
    }

    private void updateEligibility() {
        Student student = (Student) studentCombo.getSelectedItem();
        Scholarship scholarship = (Scholarship) scholarshipCombo.getSelectedItem();
        if (student == null || scholarship == null) {
            submitButton.setEnabled(false);
            eligibilityLabel.setText("Select a student and a scholarship.");
            eligibilityLabel.setForeground(Theme.MUTED);
            eligibilityCard.fill(Theme.NEUTRAL_BG).repaint();
            return;
        }

        List<String> problems = service.eligibilityProblems(student.id(), scholarship.id());
        if (problems.isEmpty()) {
            eligibilityLabel.setText("<html><body style='width:560px'><b>Eligible to apply.</b><br>"
                    + "The student meets the GPA, income, deadline and capacity requirements.</body></html>");
            eligibilityLabel.setForeground(Theme.SUCCESS);
            eligibilityCard.fill(Theme.SUCCESS_BG).repaint();
            submitButton.setEnabled(true);
        } else {
            StringBuilder html = new StringBuilder("<html><body style='width:560px'>"
                    + "<b>This application cannot be submitted yet:</b><br>");
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
        Student student = (Student) studentCombo.getSelectedItem();
        Scholarship scholarship = (Scholarship) scholarshipCombo.getSelectedItem();
        if (student == null || scholarship == null) {
            return;
        }
        try {
            Application application = service.apply(student.id(), scholarship.id(),
                    statementArea.getText());
            submitted = true;
            UiUtils.info(this, "Application " + application.id() + " was submitted for "
                    + student.fullName() + ".");
            dispose();
        } catch (ValidationException ex) {
            UiUtils.error(this, ex.getMessage());
            updateEligibility();
        }
    }

    public static boolean showDialog(Component parent, ApplicationService applicationService,
                                     StudentService studentService,
                                     ScholarshipService scholarshipService) {
        List<Student> students = studentService.all();
        List<Scholarship> scholarships = scholarshipService.all();
        if (students.isEmpty()) {
            UiUtils.info(parent, "Add a student before creating an application.");
            return false;
        }
        if (scholarships.isEmpty()) {
            UiUtils.info(parent, "Add a scholarship before creating an application.");
            return false;
        }
        ApplicationDialog dialog = new ApplicationDialog(parent, applicationService,
                students, scholarships);
        dialog.setVisible(true);
        return dialog.submitted;
    }
}
