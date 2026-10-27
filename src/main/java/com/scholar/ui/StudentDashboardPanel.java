package com.scholar.ui;

import com.scholar.model.Application;
import com.scholar.model.ApplicationStatus;
import com.scholar.model.Scholarship;
import com.scholar.model.Student;
import com.scholar.service.ApplicationService;
import com.scholar.service.ScholarshipService;
import com.scholar.service.StudentService;
import com.scholar.ui.table.StudentApplicationTableModel;
import com.scholar.util.Dates;
import com.scholar.util.Money;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.Scrollable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Student home page: funding that matches the student's record, key figures
 * and the latest office decisions on their applications.
 */
public final class StudentDashboardPanel extends JPanel implements Refreshable {

    private final StudentService students;
    private final ScholarshipService scholarships;
    private final ApplicationService applications;
    private final String studentId;

    private final JLabel greetingLabel = UiUtils.label("", Font.BOLD, 20, Theme.INK);
    private final JLabel greetingNote = UiUtils.muted(
            "Here is what is happening with your scholarship applications.");

    private final StatCard openCard = new StatCard("Open scholarships",
            "programmes accepting applications", Icons.scholarship(22, Theme.PRIMARY),
            Theme.PRIMARY_SOFT);
    private final StatCard applicationsCard = new StatCard("My applications",
            "submitted", Icons.applications(22, Theme.INFO), Theme.INFO_BG);
    private final StatCard pendingCard = new StatCard("Awaiting decision",
            "with the office", Icons.clock(22, Theme.WARNING), Theme.WARNING_BG);
    private final StatCard awardedCard = new StatCard("Total awarded",
            "funding secured", Icons.award(22, Theme.SUCCESS), Theme.SUCCESS_BG);

    private final JPanel recommendedList = new JPanel();
    private final StudentApplicationTableModel recentModel = new StudentApplicationTableModel();
    private final JTable recentTable = new JTable(recentModel);
    private final JLabel recentNote = UiUtils.muted("");

    public StudentDashboardPanel(StudentService students, ScholarshipService scholarships,
                                 ApplicationService applications, String studentId) {
        this.students = students;
        this.scholarships = scholarships;
        this.applications = applications;
        this.studentId = studentId;

        setOpaque(false);
        setLayout(new BorderLayout());

        JPanel content = new ScrollablePanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        JPanel greeting = new JPanel();
        greeting.setOpaque(false);
        greeting.setLayout(new BoxLayout(greeting, BoxLayout.Y_AXIS));
        greetingLabel.setAlignmentX(LEFT_ALIGNMENT);
        greetingNote.setAlignmentX(LEFT_ALIGNMENT);
        greeting.add(greetingLabel);
        greeting.add(Box.createVerticalStrut(3));
        greeting.add(greetingNote);
        greeting.setAlignmentX(LEFT_ALIGNMENT);
        greeting.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        greeting.setPreferredSize(new Dimension(10, 52));

        JPanel stats = new JPanel(new GridLayout(1, 4, 16, 0));
        stats.setOpaque(false);
        stats.add(openCard);
        stats.add(applicationsCard);
        stats.add(pendingCard);
        stats.add(awardedCard);
        stats.setAlignmentX(LEFT_ALIGNMENT);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 116));
        stats.setPreferredSize(new Dimension(10, 116));

        JPanel columns = new JPanel(new GridLayout(1, 2, 16, 0));
        columns.setOpaque(false);
        columns.add(buildRecommendedCard());
        columns.add(buildRecentCard());
        columns.setAlignmentX(LEFT_ALIGNMENT);
        columns.setMaximumSize(new Dimension(Integer.MAX_VALUE, 340));
        columns.setPreferredSize(new Dimension(10, 340));

        content.add(greeting);
        content.add(Box.createVerticalStrut(16));
        content.add(stats);
        content.add(Box.createVerticalStrut(16));
        content.add(columns);
        content.add(Box.createVerticalStrut(16));

        JScrollPane scroll = new JScrollPane(content,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        add(scroll, BorderLayout.CENTER);

        UiUtils.styleTable(recentTable);
        int[] widths = {85, 240, 90, 120, 100, 130};
        for (int i = 0; i < widths.length; i++) {
            recentTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        recentTable.removeColumn(recentTable.getColumnModel().getColumn(5));
        recentTable.removeColumn(recentTable.getColumnModel().getColumn(3));
        recentTable.getColumnModel().getColumn(3).setCellRenderer(UiUtils.badgeRenderer());
    }

    private RoundedPanel buildRecommendedCard() {
        RoundedPanel card = new RoundedPanel();
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 14, 20));
        card.add(header("Recommended for you",
                "Open programmes that match your record"), BorderLayout.NORTH);

        recommendedList.setOpaque(false);
        recommendedList.setLayout(new BoxLayout(recommendedList, BoxLayout.Y_AXIS));
        card.add(recommendedList, BorderLayout.CENTER);

        JLabel note = UiUtils.muted("Open Browse scholarships to see every programme.");
        note.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        card.add(note, BorderLayout.SOUTH);
        return card;
    }

    private RoundedPanel buildRecentCard() {
        RoundedPanel card = new RoundedPanel();
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 14, 20));
        card.add(header("Your latest applications",
                "The most recent decisions and submissions"), BorderLayout.NORTH);
        card.add(UiUtils.tableScroll(recentTable), BorderLayout.CENTER);
        recentNote.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        card.add(recentNote, BorderLayout.SOUTH);
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

    @Override
    public void refresh() {
        Student student = students.find(studentId);
        if (student != null) {
            greetingLabel.setText("Welcome back, " + student.firstName());
        }

        List<Scholarship> open = scholarships.open();
        openCard.setValue(Integer.toString(open.size()));
        openCard.setNote("of " + scholarships.all().size() + " programmes");

        List<Application> mine = applications.applicationsForStudent(studentId);
        long pending = mine.stream()
                .filter(a -> a.status() == ApplicationStatus.PENDING)
                .count();
        long approved = mine.stream()
                .filter(a -> a.status() == ApplicationStatus.APPROVED)
                .count();

        applicationsCard.setValue(Integer.toString(mine.size()));
        applicationsCard.setNote(approved + (approved == 1 ? " award granted" : " awards granted"));
        pendingCard.setValue(Long.toString(pending));
        pendingCard.setNote(pending == 1 ? "application with the office"
                : "applications with the office");
        awardedCard.setValue(Money.format(applications.studentTotalAwarded(studentId)));
        awardedCard.setNote("across " + approved + (approved == 1 ? " scholarship" : " scholarships"));

        Set<String> applied = mine.stream()
                .map(Application::scholarshipId)
                .collect(Collectors.toSet());
        refreshRecommended(open, applied);

        List<Application> recent = new ArrayList<>(mine);
        if (recent.size() > 4) {
            recent = recent.subList(0, 4);
        }
        recentModel.setRows(recent, applications.scholarshipMap());
        recentNote.setText(mine.isEmpty()
                ? "No applications yet. Browse scholarships to submit your first one."
                : mine.size() + " application" + (mine.size() == 1 ? "" : "s") + " in total.");
    }

    private void refreshRecommended(List<Scholarship> open, Set<String> applied) {
        recommendedList.removeAll();
        List<Scholarship> matches = open.stream()
                .filter(s -> !applied.contains(s.id()))
                .filter(s -> applications.eligibilityProblems(studentId, s.id()).isEmpty())
                .limit(4)
                .toList();

        if (matches.isEmpty()) {
            JLabel empty = UiUtils.muted("No new matches right now.");
            empty.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
            recommendedList.add(empty);
        } else {
            for (int i = 0; i < matches.size(); i++) {
                recommendedList.add(recommendedRow(matches.get(i)));
                if (i < matches.size() - 1) {
                    recommendedList.add(separator());
                }
            }
        }
        recommendedList.revalidate();
        recommendedList.repaint();
    }

    private JPanel recommendedRow(Scholarship scholarship) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(7, 0, 7, 0));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel name = UiUtils.label(scholarship.name(), Font.BOLD, 12, Theme.TEXT);
        name.setAlignmentX(LEFT_ALIGNMENT);
        JLabel meta = UiUtils.label(Money.format(scholarship.amount()) + "  •  "
                + Dates.deadlineNote(scholarship.deadline()), Font.PLAIN, 11, Theme.MUTED);
        meta.setAlignmentX(LEFT_ALIGNMENT);
        text.add(name);
        text.add(Box.createVerticalStrut(2));
        text.add(meta);

        long days = ChronoUnit.DAYS.between(LocalDate.now(), scholarship.deadline());
        Color foreground = days <= 7 ? Theme.DANGER : days <= 21 ? Theme.WARNING : Theme.SUCCESS;
        Color background = days <= 7 ? Theme.DANGER_BG
                : days <= 21 ? Theme.WARNING_BG : Theme.SUCCESS_BG;
        JLabel pillText = UiUtils.label(Dates.format(scholarship.deadline()),
                Font.BOLD, 10, foreground);
        RoundedPanel pill = new RoundedPanel().fill(background).borderColor(null)
                .shadow(false).arc(30);
        pill.setLayout(new BorderLayout());
        pill.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        pill.add(pillText, BorderLayout.CENTER);

        row.add(text, BorderLayout.CENTER);
        row.add(pill, BorderLayout.EAST);

        row.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Student student = students.find(studentId);
                if (student == null) {
                    return;
                }
                if (StudentApplyDialog.showDialog(StudentDashboardPanel.this, applications,
                        student, scholarship)) {
                    refresh();
                }
            }
        });
        return row;
    }

    private static JPanel separator() {
        JPanel line = new JPanel();
        line.setBackground(Theme.BORDER);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        line.setPreferredSize(new Dimension(10, 1));
        return line;
    }

    /** Panel that fills the viewport width while allowing vertical scrolling. */
    private static final class ScrollablePanel extends JPanel implements Scrollable {

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(18, visibleRect.height - 40);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}
