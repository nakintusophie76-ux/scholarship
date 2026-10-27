package com.scholar.ui;

import com.scholar.model.ApplicationStatus;
import com.scholar.model.Scholarship;
import com.scholar.model.StudentStatus;
import com.scholar.service.ApplicationService;
import com.scholar.service.ReportService;
import com.scholar.service.ScholarshipService;
import com.scholar.service.StudentService;
import com.scholar.store.DataStore;
import com.scholar.ui.table.ScholarshipReportTableModel;
import com.scholar.util.Money;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Reports page: live award summary and one-click CSV exports.
 */
public final class ReportsPanel extends JPanel implements Refreshable {

    private final ReportService reports;
    private final StudentService students;
    private final ScholarshipService scholarships;
    private final ApplicationService applications;
    private final DataStore store;

    private final StatCard studentCard = new StatCard("Students",
            "registered", Icons.students(22, Theme.PRIMARY), Theme.PRIMARY_SOFT);
    private final StatCard openCard = new StatCard("Open scholarships",
            "programmes", Icons.scholarship(22, Theme.SUCCESS), Theme.SUCCESS_BG);
    private final StatCard approvedCard = new StatCard("Approved awards",
            "decisions made", Icons.check(22, Theme.INFO), Theme.INFO_BG);
    private final StatCard disbursedCard = new StatCard("Disbursed",
            "committed funding", Icons.award(22, Theme.WARNING), Theme.WARNING_BG);

    private final ScholarshipReportTableModel model = new ScholarshipReportTableModel();
    private final JTable table = new JTable(model);
    private final JLabel footer = UiUtils.muted("Loading…");

    public ReportsPanel(ReportService reports, StudentService students,
                        ScholarshipService scholarships, ApplicationService applications,
                        DataStore store) {
        this.reports = reports;
        this.students = students;
        this.scholarships = scholarships;
        this.applications = applications;
        this.store = store;

        setOpaque(false);
        setLayout(new BorderLayout(0, 14));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JPanel stats = new JPanel(new GridLayout(1, 4, 16, 0));
        stats.setOpaque(false);
        stats.add(studentCard);
        stats.add(openCard);
        stats.add(approvedCard);
        stats.add(disbursedCard);
        stats.setAlignmentX(LEFT_ALIGNMENT);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 116));
        stats.setPreferredSize(new Dimension(10, 116));

        top.add(stats);
        top.add(Box.createVerticalStrut(12));
        top.add(buildExportBar());

        add(top, BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);

        UiUtils.styleTable(table);
        int[] widths = {280, 100, 90, 90, 90, 150};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.getColumnModel().getColumn(5).setCellRenderer(UiUtils.moneyRenderer());
    }

    private JPanel buildExportBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setOpaque(false);
        bar.setAlignmentX(LEFT_ALIGNMENT);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JLabel caption = UiUtils.label("Download CSV reports", Font.BOLD, 13, Theme.TEXT);
        bar.add(caption, BorderLayout.WEST);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);

        JButton studentExport = UiUtils.secondaryButton("Students CSV");
        studentExport.addActionListener(e -> export("Export students", "students-",
                reports::exportStudents));
        JButton applicationExport = UiUtils.secondaryButton("Applications CSV");
        applicationExport.addActionListener(e -> export("Export applications", "applications-",
                reports::exportApplications));
        JButton awardExport = UiUtils.secondaryButton("Awards CSV");
        awardExport.addActionListener(e -> export("Export awards", "awards-",
                reports::exportAwards));
        JButton folder = UiUtils.ghostButton("Open data folder");
        folder.addActionListener(e -> openDataFolder());

        buttons.add(studentExport);
        buttons.add(applicationExport);
        buttons.add(awardExport);
        buttons.add(folder);
        bar.add(buttons, BorderLayout.EAST);
        return bar;
    }

    private RoundedPanel buildTableCard() {
        RoundedPanel card = new RoundedPanel();
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 12, 20));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel title = UiUtils.label("Awards by scholarship", Font.BOLD, 15, Theme.TEXT);
        title.setAlignmentX(LEFT_ALIGNMENT);
        JLabel subtitle = UiUtils.label("Places used and total amount committed per programme",
                Font.PLAIN, 12, Theme.MUTED);
        subtitle.setAlignmentX(LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(2));
        header.add(subtitle);

        card.add(header, BorderLayout.NORTH);
        card.add(UiUtils.tableScroll(table), BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    @Override
    public void refresh() {
        List<com.scholar.model.Student> allStudents = students.all();
        studentCard.setValue(Integer.toString(allStudents.size()));
        studentCard.setNote(students.count(StudentStatus.ACTIVE) + " active");

        List<Scholarship> allScholarships = scholarships.all();
        openCard.setValue(Long.toString(scholarships.countOpen()));
        openCard.setNote(allScholarships.size() + " programmes in total");

        Map<ApplicationStatus, Long> counts = applications.statusCounts();
        long approved = counts.getOrDefault(ApplicationStatus.APPROVED, 0L);
        approvedCard.setValue(Long.toString(approved));
        approvedCard.setNote(counts.getOrDefault(ApplicationStatus.PENDING, 0L) + " pending reviews");

        double total = applications.totalAwarded();
        disbursedCard.setValue(Money.format(total));
        disbursedCard.setNote("committed funding");

        model.setRows(reports.awardsByScholarship());
        long usedPlaces = reports.awardsByScholarship().stream()
                .mapToLong(ReportService.ScholarshipReport::approved).sum();
        footer.setText(allScholarships.size() + " programmes  •  " + usedPlaces
                + " places awarded  •  " + Money.format(total) + " total committed");
    }

    private void export(String title, String filePrefix, ExportAction action) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(title);
        chooser.setSelectedFile(new File(System.getProperty("user.home"),
                filePrefix + LocalDate.now().format(DateTimeFormatter.ISO_DATE) + ".csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path target = chooser.getSelectedFile().toPath();
        if (!target.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            target = target.resolveSibling(target.getFileName() + ".csv");
        }
        if (Files.exists(target)
                && !UiUtils.confirm(this, "Replace file",
                target + "\nalready exists. Replace it?")) {
            return;
        }

        try {
            int rows = action.write(target);
            UiUtils.info(this, rows + " rows exported to\n" + target);
        } catch (IOException ex) {
            UiUtils.error(this, "Could not write the file:\n" + ex.getMessage());
        }
    }

    private void openDataFolder() {
        try {
            if (!Desktop.isDesktopSupported()) {
                throw new UnsupportedOperationException("Desktop integration is unavailable.");
            }
            Desktop.getDesktop().open(store.directory().toFile());
        } catch (IOException | UnsupportedOperationException ex) {
            UiUtils.info(this, "Data files are stored in:\n" + store.directory());
        }
    }

    @FunctionalInterface
    private interface ExportAction {
        int write(Path file) throws IOException;
    }
}
