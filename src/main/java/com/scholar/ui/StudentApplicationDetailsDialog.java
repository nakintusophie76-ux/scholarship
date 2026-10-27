package com.scholar.ui;

import com.scholar.model.Application;
import com.scholar.model.Scholarship;
import com.scholar.service.ApplicationService;
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

/**
 * Read-only view of one of the student's applications, including the office
 * decision and review notes.
 */
public final class StudentApplicationDetailsDialog extends BaseDialog {

    private StudentApplicationDetailsDialog(Component parent, ApplicationService service,
                                             Application application) {
        super(parent, "Application " + application.id(),
                "Submitted " + Dates.format(application.submittedAt())
                        + "  •  " + application.status().label(), 660);

        Scholarship scholarship = service.scholarshipMap().get(application.scholarshipId());

        JPanel info = new JPanel(new GridBagLayout());
        info.setOpaque(false);
        addInfo(info, 0, 0, "Scholarship", scholarship == null
                ? application.scholarshipId() : scholarship.name());
        addInfo(info, 1, 0, "Award", scholarship == null ? "—"
                : Money.format(scholarship.amount()));
        addInfo(info, 0, 1, "Status", application.status().label());
        addInfo(info, 1, 1, "Decision date", application.reviewedAt() == null
                ? "Awaiting review" : Dates.format(application.reviewedAt()));

        JTextArea statementArea = readOnlyArea(application.statement());
        JScrollPane statementScroll = new JScrollPane(statementArea);
        statementScroll.setPreferredSize(new Dimension(0, 90));
        statementScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1, true));

        String notes = application.reviewNotes() == null || application.reviewNotes().isBlank()
                ? "No review notes have been added yet."
                : application.reviewNotes();
        JTextArea notesArea = readOnlyArea(notes);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setPreferredSize(new Dimension(0, 78));
        notesScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1, true));

        FormPanel form = new FormPanel();
        form.addFull(info);
        form.addFull(UiUtils.labeled("Your statement", statementScroll));
        form.addFull(UiUtils.labeled("Office decision notes", notesScroll));

        RoundedPanel card = new RoundedPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 8, 20));
        card.add(form, BorderLayout.CENTER);
        setBody(card);

        JButton close = UiUtils.primaryButton("Close");
        close.addActionListener(e -> dispose());
        setButtons(close);
        getRootPane().setDefaultButton(close);

        finish();
    }

    private static JTextArea readOnlyArea(String text) {
        JTextArea area = new JTextArea(text == null ? "" : text);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(Theme.font(Font.PLAIN, 13));
        area.setForeground(Theme.TEXT);
        area.setBackground(Theme.ROW_ALT);
        area.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        return area;
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

    public static void showDialog(Component parent, ApplicationService service,
                                  Application application) {
        new StudentApplicationDetailsDialog(parent, service, application).setVisible(true);
    }
}
