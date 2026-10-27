package com.scholar.ui;

import com.scholar.model.Application;
import com.scholar.model.ApplicationStatus;
import com.scholar.model.Scholarship;
import com.scholar.service.ApplicationService;
import com.scholar.ui.table.StudentApplicationTableModel;
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
import java.util.List;
import java.util.Map;

/**
 * The signed in student's application history and office decisions.
 */
public final class StudentApplicationsPanel extends JPanel implements Refreshable {

    private final ApplicationService service;
    private final String studentId;

    private final StudentApplicationTableModel model = new StudentApplicationTableModel();
    private final JTable table = new JTable(model);
    private final TableRowSorter<StudentApplicationTableModel> sorter =
            new TableRowSorter<>(model);
    private final JTextField searchField =
            UiUtils.searchField("Search your applications…");
    private final JComboBox<String> statusFilter = UiUtils.comboBox(new String[]{
            "All statuses", "Pending", "Approved", "Waitlisted", "Rejected"});
    private final JLabel footer = UiUtils.muted("Loading…");
    private final JButton detailsButton = UiUtils.primaryButton("View details");

    public StudentApplicationsPanel(ApplicationService service, String studentId) {
        this.service = service;
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
        statusFilter.addActionListener(e -> applyFilter());
        table.getSelectionModel().addListSelectionListener(e -> updateButtons());
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showSelected();
                }
            }
        });

        detailsButton.addActionListener(e -> showSelected());
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
        right.add(detailsButton);

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
        panel.add(UiUtils.rightAligned("Double-click an application to read the decision"),
                BorderLayout.EAST);
        return panel;
    }

    private void configureColumns() {
        int[] widths = {100, 240, 100, 160, 120, 170};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.getColumnModel().getColumn(2).setCellRenderer(UiUtils.moneyRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(UiUtils.badgeRenderer());
    }

    private void applyFilter() {
        String query = searchField.getText() == null
                ? "" : searchField.getText().trim().toLowerCase();
        ApplicationStatus status = switch (statusFilter.getSelectedIndex()) {
            case 1 -> ApplicationStatus.PENDING;
            case 2 -> ApplicationStatus.APPROVED;
            case 3 -> ApplicationStatus.WAITLISTED;
            case 4 -> ApplicationStatus.REJECTED;
            default -> null;
        };
        Map<String, Scholarship> scholarships = service.scholarshipMap();

        sorter.setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends StudentApplicationTableModel,
                    ? extends Integer> entry) {
                Application application = model.get(entry.getIdentifier());
                if (status != null && application.status() != status) {
                    return false;
                }
                if (query.isEmpty()) {
                    return true;
                }
                Scholarship scholarship = scholarships.get(application.scholarshipId());
                return application.id().toLowerCase().contains(query)
                        || (scholarship != null
                        && scholarship.name().toLowerCase().contains(query));
            }
        });
        updateFooter();
        updateButtons();
    }

    private void updateFooter() {
        List<Application> all = service.applicationsForStudent(studentId);
        long pending = all.stream()
                .filter(a -> a.status() == ApplicationStatus.PENDING)
                .count();
        footer.setText(table.getRowCount() + " of " + all.size()
                + " applications shown  •  " + pending + " pending  •  "
                + Money.format(service.studentTotalAwarded(studentId)) + " awarded");
    }

    private void updateButtons() {
        detailsButton.setEnabled(table.getSelectedRow() >= 0);
    }

    private Application selectedApplication() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        return model.get(table.convertRowIndexToModel(viewRow));
    }

    private void showSelected() {
        Application application = selectedApplication();
        if (application == null) {
            return;
        }
        StudentApplicationDetailsDialog.showDialog(this, service, application);
    }

    @Override
    public void refresh() {
        model.setRows(service.applicationsForStudent(studentId), service.scholarshipMap());
        applyFilter();
        updateButtons();
    }
}
