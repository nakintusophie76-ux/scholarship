package com.scholar.ui;

import com.scholar.model.Application;
import com.scholar.model.ApplicationStatus;
import com.scholar.model.Scholarship;
import com.scholar.model.Student;
import com.scholar.service.ApplicationService;
import com.scholar.service.ScholarshipService;
import com.scholar.service.StudentService;
import com.scholar.service.ValidationException;
import com.scholar.ui.table.ApplicationTableModel;
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
import java.util.Map;

/**
 * Application queue with live filtering and the review workflow.
 */
public final class ApplicationsPanel extends JPanel implements Refreshable {

    private final ApplicationService service;
    private final StudentService studentService;
    private final ScholarshipService scholarshipService;

    private final ApplicationTableModel model = new ApplicationTableModel();
    private final JTable table = new JTable(model);
    private final TableRowSorter<ApplicationTableModel> sorter = new TableRowSorter<>(model);
    private final JTextField searchField = UiUtils.searchField("Search student or scholarship…");
    private final JComboBox<String> statusFilter = UiUtils.comboBox(new String[]{
            "All statuses", "Pending", "Approved", "Waitlisted", "Rejected"});
    private final JLabel footer = UiUtils.muted("Loading…");
    private final JButton reviewButton = UiUtils.secondaryButton("Review / award");
    private final JButton deleteButton = UiUtils.dangerButton("Delete");

    public ApplicationsPanel(ApplicationService service, StudentService studentService,
                             ScholarshipService scholarshipService) {
        this.service = service;
        this.studentService = studentService;
        this.scholarshipService = scholarshipService;

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
        statusFilter.addActionListener(e -> applyFilter());
        table.getSelectionModel().addListSelectionListener(e -> updateButtons());
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    reviewSelected();
                }
            }
        });

        reviewButton.addActionListener(e -> reviewSelected());
        deleteButton.addActionListener(e -> deleteSelected());
        updateButtons();
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        statusFilter.setPreferredSize(new java.awt.Dimension(150, 38));
        left.add(searchField);
        left.add(statusFilter);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JButton add = UiUtils.primaryButton("+ New application");
        add.addActionListener(e -> newApplication());
        right.add(reviewButton);
        right.add(deleteButton);
        right.add(add);

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
        panel.add(UiUtils.rightAligned("Double-click a row to review and award"), BorderLayout.EAST);
        return panel;
    }

    private void configureColumns() {
        int[] widths = {100, 200, 280, 150, 110};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.getColumnModel().getColumn(4).setCellRenderer(UiUtils.badgeRenderer());
    }

    private void applyFilter() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        ApplicationStatus status = switch (statusFilter.getSelectedIndex()) {
            case 1 -> ApplicationStatus.PENDING;
            case 2 -> ApplicationStatus.APPROVED;
            case 3 -> ApplicationStatus.WAITLISTED;
            case 4 -> ApplicationStatus.REJECTED;
            default -> null;
        };
        Map<String, Student> students = service.studentMap();
        Map<String, Scholarship> scholarships = service.scholarshipMap();

        sorter.setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends ApplicationTableModel, ? extends Integer> entry) {
                Application application = model.get(entry.getIdentifier());
                if (status != null && application.status() != status) {
                    return false;
                }
                if (query.isEmpty()) {
                    return true;
                }
                Student student = students.get(application.studentId());
                Scholarship scholarship = scholarships.get(application.scholarshipId());
                return application.id().toLowerCase().contains(query)
                        || (student != null && student.fullName().toLowerCase().contains(query))
                        || (scholarship != null && scholarship.name().toLowerCase().contains(query));
            }
        });
        updateFooter();
        updateButtons();
    }

    private void updateFooter() {
        Map<ApplicationStatus, Long> counts = service.statusCounts();
        footer.setText(table.getRowCount() + " of " + model.getRowCount()
                + " applications shown  •  " + counts.getOrDefault(ApplicationStatus.PENDING, 0L)
                + " pending  •  " + Money.format(service.totalAwarded()) + " awarded");
    }

    private void updateButtons() {
        boolean selected = table.getSelectedRow() >= 0;
        reviewButton.setEnabled(selected);
        deleteButton.setEnabled(selected);
    }

    private Application selectedApplication() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        return model.get(table.convertRowIndexToModel(viewRow));
    }

    private void newApplication() {
        if (ApplicationDialog.showDialog(this, service, studentService, scholarshipService)) {
            refresh();
        }
    }

    private void reviewSelected() {
        Application application = selectedApplication();
        if (application == null) {
            return;
        }
        if (ReviewDialog.showDialog(this, service, application)) {
            refresh();
        }
    }

    private void deleteSelected() {
        Application application = selectedApplication();
        if (application == null) {
            return;
        }
        boolean confirmed = UiUtils.confirm(this, "Delete application",
                "Delete application " + application.id() + "?\nThis action cannot be undone.");
        if (!confirmed) {
            return;
        }
        try {
            service.delete(application.id());
            refresh();
        } catch (ValidationException ex) {
            UiUtils.error(this, ex.getMessage());
        }
    }

    @Override
    public void refresh() {
        model.setRows(service.all(), service.studentMap(), service.scholarshipMap());
        applyFilter();
    }
}
