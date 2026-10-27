package com.scholar.ui;

import com.scholar.model.Application;
import com.scholar.model.Scholarship;
import com.scholar.model.Student;
import com.scholar.service.ApplicationService;
import com.scholar.service.ScholarshipService;
import com.scholar.service.StudentService;
import com.scholar.ui.table.StudentScholarshipTableModel;
import com.scholar.util.Money;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Student catalogue: open and closed programmes with a live eligibility badge.
 */
public final class StudentScholarshipsPanel extends JPanel implements Refreshable {

    private final StudentService studentService;
    private final ScholarshipService scholarshipService;
    private final ApplicationService applications;
    private final String studentId;

    private final StudentScholarshipTableModel model = new StudentScholarshipTableModel();
    private final JTable table = new JTable(model);
    private final TableRowSorter<StudentScholarshipTableModel> sorter =
            new TableRowSorter<>(model);
    private final JTextField searchField =
            UiUtils.searchField("Search scholarship, sponsor or type…");
    private final JComboBox<String> filterCombo =
            UiUtils.comboBox(new String[]{"Open programmes", "All programmes"});
    private final JLabel footer = UiUtils.muted("Loading…");
    private final JButton detailsButton = UiUtils.secondaryButton("View details");
    private final JButton applyButton = UiUtils.primaryButton("Apply");

    private Map<String, String> statuses = new HashMap<>();

    public StudentScholarshipsPanel(StudentService studentService,
                                    ScholarshipService scholarshipService,
                                    ApplicationService applications, String studentId) {
        this.studentService = studentService;
        this.scholarshipService = scholarshipService;
        this.applications = applications;
        this.studentId = studentId;

        setOpaque(false);
        setLayout(new BorderLayout(0, 14));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        table.setRowSorter(sorter);
        configureColumns();

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }
        });
        filterCombo.addActionListener(e -> applyFilter());
        table.getSelectionModel().addListSelectionListener(e -> updateButtons());
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelected();
                }
            }
        });

        detailsButton.addActionListener(e -> showDetails());
        applyButton.addActionListener(e -> openSelected());
        updateButtons();
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        filterCombo.setPreferredSize(new java.awt.Dimension(160, 38));
        left.add(searchField);
        left.add(filterCombo);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(detailsButton);
        right.add(applyButton);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildTableCard() {
        UiUtils.styleTable(table);
        RoundedPanel card = new RoundedPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        card.add(UiUtils.tableScroll(table), BorderLayout.CENTER);
        return card;
    }

    private JPanel buildFooter() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        panel.add(footer, BorderLayout.WEST);
        panel.add(UiUtils.rightAligned("Double-click a programme for details or to apply"),
                BorderLayout.EAST);
        return panel;
    }

    private void configureColumns() {
        int[] widths = {90, 230, 90, 90, 75, 105, 150, 100};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.getColumnModel().getColumn(3).setCellRenderer(UiUtils.moneyRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(UiUtils.gpaRenderer());
        table.getColumnModel().getColumn(7).setCellRenderer(UiUtils.badgeRenderer());
    }

    private void applyFilter() {
        String query = searchField.getText() == null
                ? "" : searchField.getText().trim().toLowerCase();
        boolean openOnly = filterCombo.getSelectedIndex() == 0;
        LocalDate today = LocalDate.now();

        sorter.setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends StudentScholarshipTableModel,
                    ? extends Integer> entry) {
                Scholarship scholarship = model.get(entry.getIdentifier());
                boolean matchesStatus = !openOnly || scholarship.isOpen(today);
                boolean matchesQuery = query.isEmpty()
                        || scholarship.name().toLowerCase().contains(query)
                        || scholarship.sponsor().toLowerCase().contains(query)
                        || scholarship.type().label().toLowerCase().contains(query)
                        || scholarship.id().toLowerCase().contains(query);
                return matchesStatus && matchesQuery;
            }
        });
        updateFooter();
        updateButtons();
    }

    private void updateFooter() {
        long open = scholarshipService.countOpen();
        long eligible = statuses.values().stream()
                .filter("Eligible"::equals)
                .count();
        footer.setText(table.getRowCount() + " of " + model.getRowCount()
                + " programmes shown  •  " + open + " open  •  "
                + eligible + " match your record");
    }

    private void updateButtons() {
        Scholarship scholarship = selectedScholarship();
        boolean selected = scholarship != null;
        detailsButton.setEnabled(selected);
        applyButton.setEnabled(selected && "Eligible".equals(model.eligibilityOf(scholarship)));
    }

    private Scholarship selectedScholarship() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        return model.get(table.convertRowIndexToModel(viewRow));
    }

    private void showDetails() {
        Scholarship scholarship = selectedScholarship();
        if (scholarship == null) {
            return;
        }
        Student student = studentService.find(studentId);
        String status = model.eligibilityOf(scholarship);
        StringBuilder message = new StringBuilder()
                .append(scholarship.name()).append("\n")
                .append(scholarship.sponsor()).append("\n\n")
                .append(scholarship.description()).append("\n\n")
                .append("Award: ").append(Money.format(scholarship.amount())).append("\n")
                .append("Deadline: ").append(com.scholar.util.Dates.format(
                        scholarship.deadline())).append("\n")
                .append("Minimum GPA: ").append(Money.gpa(scholarship.minGpa())).append("\n")
                .append("Income limit: ").append(scholarship.hasIncomeLimit()
                        ? Money.format(scholarship.maxIncome()) : "No limit").append("\n\n")
                .append("Eligibility: ").append(status);

        if ("Not eligible".equals(status) && student != null) {
            List<String> problems = applications.eligibilityProblems(student.id(),
                    scholarship.id());
            if (!problems.isEmpty()) {
                message.append("\n\nWhy you cannot apply:");
                for (String problem : problems) {
                    message.append("\n• ").append(problem);
                }
            }
        }
        UiUtils.info(this, message.toString());
    }

    private void openSelected() {
        Scholarship scholarship = selectedScholarship();
        if (scholarship == null) {
            return;
        }
        String status = model.eligibilityOf(scholarship);
        Student student = studentService.find(studentId);
        if (student == null) {
            UiUtils.error(this, "Your student record no longer exists. Contact the office.");
            return;
        }
        if ("Applied".equals(status)) {
            UiUtils.info(this, "You have already applied for " + scholarship.name()
                    + ".\nTrack the decision under My applications.");
            return;
        }
        if ("Eligible".equals(status)) {
            if (StudentApplyDialog.showDialog(this, applications, student, scholarship)) {
                refresh();
            }
            return;
        }
        List<String> problems = applications.eligibilityProblems(student.id(),
                scholarship.id());
        StringBuilder message = new StringBuilder("You cannot apply for ")
                .append(scholarship.name()).append(":");
        for (String problem : problems) {
            message.append("\n• ").append(problem);
        }
        UiUtils.error(this, message.toString());
    }

    @Override
    public void refresh() {
        List<Scholarship> all = scholarshipService.all();
        Set<String> applied = applications.applicationsForStudent(studentId).stream()
                .map(Application::scholarshipId)
                .collect(Collectors.toSet());

        Map<String, Integer> approved = new HashMap<>();
        Map<String, String> calculated = new HashMap<>();
        for (Scholarship scholarship : all) {
            approved.put(scholarship.id(), (int) applications.approvedCount(scholarship.id()));
            if (applied.contains(scholarship.id())) {
                calculated.put(scholarship.id(), "Applied");
            } else if (applications.eligibilityProblems(studentId, scholarship.id()).isEmpty()) {
                calculated.put(scholarship.id(), "Eligible");
            } else {
                calculated.put(scholarship.id(), "Not eligible");
            }
        }
        statuses = calculated;
        model.setRows(all, approved, calculated);
        applyFilter();
        updateButtons();
    }
}
