package com.scholar.ui;

import com.scholar.model.ApplicationStatus;
import com.scholar.model.Scholarship;
import com.scholar.model.Student;
import com.scholar.model.StudentStatus;
import com.scholar.service.ApplicationService;
import com.scholar.service.ScholarshipService;
import com.scholar.service.StudentService;
import com.scholar.ui.table.ApplicationTableModel;
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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Overview page: key figures, application pipeline, upcoming deadlines and
 * the latest submissions.
 */
public final class DashboardPanel extends JPanel implements Refreshable {

    private final StudentService students;
    private final ScholarshipService scholarships;
    private final ApplicationService applications;

    private final StatCard studentCard = new StatCard("Students",
            "active records", Icons.students(22, Theme.PRIMARY), Theme.PRIMARY_SOFT);
    private final StatCard scholarshipCard = new StatCard("Open scholarships",
            "programmes", Icons.scholarship(22, Theme.SUCCESS), Theme.SUCCESS_BG);
    private final StatCard pendingCard = new StatCard("Pending reviews",
            "awaiting a decision", Icons.clock(22, Theme.WARNING), Theme.WARNING_BG);
    private final StatCard awardCard = new StatCard("Total awarded",
            "awards granted", Icons.award(22, Theme.INFO), Theme.INFO_BG);

    private final StatusChart chart = new StatusChart();
    private final JPanel deadlineList = new JPanel();
    private final ApplicationTableModel recentModel = new ApplicationTableModel();
    private final JTable recentTable = new JTable(recentModel);

    public DashboardPanel(StudentService students, ScholarshipService scholarships,
                          ApplicationService applications) {
        this.students = students;
        this.scholarships = scholarships;
        this.applications = applications;

        setOpaque(false);
        setLayout(new BorderLayout());

        JPanel content = new ScrollablePanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        JPanel stats = new JPanel(new GridLayout(1, 4, 16, 0));
        stats.setOpaque(false);
        stats.add(studentCard);
        stats.add(scholarshipCard);
        stats.add(pendingCard);
        stats.add(awardCard);
        stats.setAlignmentX(LEFT_ALIGNMENT);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 116));
        stats.setPreferredSize(new Dimension(10, 116));

        JPanel charts = new JPanel(new GridLayout(1, 2, 16, 0));
        charts.setOpaque(false);
        charts.add(buildChartCard());
        charts.add(buildDeadlineCard());
        charts.setAlignmentX(LEFT_ALIGNMENT);
        charts.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        charts.setPreferredSize(new Dimension(10, 300));

        RoundedPanel recent = buildRecentCard();
        recent.setAlignmentX(LEFT_ALIGNMENT);
        recent.setMaximumSize(new Dimension(Integer.MAX_VALUE, 340));
        recent.setPreferredSize(new Dimension(10, 340));

        content.add(stats);
        content.add(Box.createVerticalStrut(16));
        content.add(charts);
        content.add(Box.createVerticalStrut(16));
        content.add(recent);

        JScrollPane scroll = new JScrollPane(content,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        add(scroll, BorderLayout.CENTER);

        UiUtils.styleTable(recentTable);
        int[] widths = {100, 180, 250, 150, 110};
        for (int i = 0; i < widths.length; i++) {
            recentTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        recentTable.getColumnModel().getColumn(4).setCellRenderer(UiUtils.badgeRenderer());
    }

    private RoundedPanel buildChartCard() {
        RoundedPanel card = new RoundedPanel();
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 16, 20));
        card.add(header("Applications by status",
                "A snapshot of the current review pipeline"), BorderLayout.NORTH);
        card.add(chart, BorderLayout.CENTER);
        return card;
    }

    private RoundedPanel buildDeadlineCard() {
        RoundedPanel card = new RoundedPanel();
        card.setLayout(new BorderLayout(0, 6));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 14, 20));
        card.add(header("Upcoming deadlines",
                "Open programmes ordered by closing date"), BorderLayout.NORTH);

        deadlineList.setOpaque(false);
        deadlineList.setLayout(new BoxLayout(deadlineList, BoxLayout.Y_AXIS));
        card.add(deadlineList, BorderLayout.CENTER);
        return card;
    }

    private RoundedPanel buildRecentCard() {
        RoundedPanel card = new RoundedPanel();
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 12, 20));
        card.add(header("Recent applications",
                "The five most recent submissions across all programmes"), BorderLayout.NORTH);
        card.add(UiUtils.tableScroll(recentTable), BorderLayout.CENTER);
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
        List<Student> allStudents = students.all();
        studentCard.setValue(Integer.toString(allStudents.size()));
        studentCard.setNote(students.count(StudentStatus.ACTIVE) + " active records");

        List<Scholarship> open = scholarships.open();
        scholarshipCard.setValue(Integer.toString(open.size()));
        scholarshipCard.setNote(scholarships.all().size() + " programmes in total");

        Map<ApplicationStatus, Long> counts = applications.statusCounts();
        pendingCard.setValue(Long.toString(counts.getOrDefault(ApplicationStatus.PENDING, 0L)));
        pendingCard.setNote("awaiting a decision");

        double awarded = applications.totalAwarded();
        awardCard.setValue(Money.format(awarded));
        long approved = counts.getOrDefault(ApplicationStatus.APPROVED, 0L);
        awardCard.setNote(approved + (approved == 1 ? " award granted" : " awards granted"));

        chart.setCounts(counts);
        refreshDeadlines(open);
        recentModel.setRows(applications.recent(5),
                applications.studentMap(), applications.scholarshipMap());
    }

    private void refreshDeadlines(List<Scholarship> open) {
        deadlineList.removeAll();
        if (open.isEmpty()) {
            JLabel empty = UiUtils.muted("No open scholarships right now.");
            empty.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
            deadlineList.add(empty);
        } else {
            int limit = Math.min(4, open.size());
            for (int i = 0; i < limit; i++) {
                deadlineList.add(deadlineRow(open.get(i)));
                if (i < limit - 1) {
                    deadlineList.add(separator());
                }
            }
        }
        deadlineList.revalidate();
        deadlineList.repaint();
    }

    private JPanel deadlineRow(Scholarship scholarship) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(7, 0, 7, 0));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel name = UiUtils.label(scholarship.name(), Font.BOLD, 12, Theme.TEXT);
        name.setAlignmentX(LEFT_ALIGNMENT);
        JLabel meta = UiUtils.label(Money.format(scholarship.amount()) + "  •  "
                + Dates.format(scholarship.deadline()), Font.PLAIN, 11, Theme.MUTED);
        meta.setAlignmentX(LEFT_ALIGNMENT);
        text.add(name);
        text.add(Box.createVerticalStrut(2));
        text.add(meta);

        long days = ChronoUnit.DAYS.between(LocalDate.now(), scholarship.deadline());
        Color foreground;
        Color background;
        if (days <= 7) {
            foreground = Theme.DANGER;
            background = Theme.DANGER_BG;
        } else if (days <= 21) {
            foreground = Theme.WARNING;
            background = Theme.WARNING_BG;
        } else {
            foreground = Theme.SUCCESS;
            background = Theme.SUCCESS_BG;
        }

        JLabel note = UiUtils.label(Dates.deadlineNote(scholarship.deadline()),
                Font.BOLD, 10, foreground);
        RoundedPanel pill = new RoundedPanel().fill(background).borderColor(null).shadow(false).arc(30);
        pill.setLayout(new BorderLayout());
        pill.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        pill.add(note, BorderLayout.CENTER);

        row.add(text, BorderLayout.CENTER);
        row.add(pill, BorderLayout.EAST);
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

    /** Horizontal bars for Pending / Approved / Waitlisted / Rejected. */
    private static final class StatusChart extends JPanel {

        private Map<ApplicationStatus, Long> counts = new EnumMap<>(ApplicationStatus.class);

        StatusChart() {
            setOpaque(false);
        }

        void setCounts(Map<ApplicationStatus, Long> counts) {
            this.counts = new EnumMap<>(counts);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            ApplicationStatus[] statuses = ApplicationStatus.values();
            double max = Math.max(1, counts.values().stream()
                    .mapToLong(Long::longValue).max().orElse(1));
            int labelWidth = 84;
            int valueWidth = 34;
            int rowHeight = Math.max(26, (getHeight() - 8) / statuses.length);

            for (int i = 0; i < statuses.length; i++) {
                ApplicationStatus status = statuses[i];
                long count = counts.getOrDefault(status, 0L);
                int y = 4 + i * rowHeight;
                int centerY = y + rowHeight / 2;

                g2.setFont(Theme.font(Font.PLAIN, 12));
                g2.setColor(Theme.MUTED);
                g2.drawString(status.label(), 0, centerY + 4);

                int trackX = labelWidth;
                int trackWidth = Math.max(10, getWidth() - labelWidth - valueWidth);
                int barHeight = 12;

                g2.setColor(Theme.BG);
                g2.fillRoundRect(trackX, centerY - barHeight / 2, trackWidth, barHeight,
                        barHeight, barHeight);

                if (count > 0) {
                    int filled = (int) Math.round(trackWidth * (count / max));
                    filled = Math.max(barHeight, filled);
                    g2.setColor(chartColor(status));
                    g2.fillRoundRect(trackX, centerY - barHeight / 2, filled, barHeight,
                            barHeight, barHeight);
                }

                g2.setColor(Theme.TEXT);
                g2.setFont(Theme.font(Font.BOLD, 12));
                String text = Long.toString(count);
                int width = g2.getFontMetrics().stringWidth(text);
                g2.drawString(text, getWidth() - width, centerY + 4);
            }
            g2.dispose();
        }

        private static Color chartColor(ApplicationStatus status) {
            return switch (status) {
                case PENDING -> Theme.CHART_1;
                case APPROVED -> Theme.CHART_2;
                case WAITLISTED -> Theme.CHART_3;
                case REJECTED -> Theme.CHART_4;
            };
        }
    }
}
