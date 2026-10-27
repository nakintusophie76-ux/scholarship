package com.scholar.ui;

import com.scholar.model.Student;
import com.scholar.model.StudentStatus;
import com.scholar.service.ApplicationService;
import com.scholar.service.StudentService;
import com.scholar.service.ValidationException;
import com.scholar.ui.table.StudentTableModel;

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

/**
 * Student directory with live search, status filtering and CRUD actions.
 */
public final class StudentsPanel extends JPanel implements Refreshable {

    private final StudentService service;
    private final StudentTableModel model = new StudentTableModel();
    private final JTable table = new JTable(model);
    private final TableRowSorter<StudentTableModel> sorter = new TableRowSorter<>(model);
    private final JTextField searchField = UiUtils.searchField("Search name, email, programme…");
    private final JComboBox<String> statusFilter =
            UiUtils.comboBox(new String[]{"All statuses", "Active", "Inactive", "Graduated"});
    private final JLabel footer = UiUtils.muted("Loading…");
    private final JButton editButton = UiUtils.secondaryButton("Edit");
    private final JButton deleteButton = UiUtils.dangerButton("Delete");

    public StudentsPanel(StudentService service, ApplicationService applicationService) {
        this.service = service;
        setOpaque(false);
        setLayout(new BorderLayout(0, 14));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        table.setRowSorter(sorter);
        configureColumns();
        sorter.setSortKeys(List.of(new javax.swing.table.TableRowSorter.SortKey(1,
                javax.swing.SortOrder.ASCENDING)));

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
                    editSelected();
                }
            }
        });

        editButton.addActionListener(e -> editSelected());
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
        JButton add = UiUtils.primaryButton("+ Add student");
        add.addActionListener(e -> addStudent());
        right.add(editButton);
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
        JLabel hint = UiUtils.rightAligned("Double-click a row to edit");
        panel.add(hint, BorderLayout.EAST);
        return panel;
    }

    private void configureColumns() {
        int[] widths = {90, 170, 210, 170, 60, 70, 120, 95};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.getColumnModel().getColumn(5).setCellRenderer(UiUtils.gpaRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(UiUtils.moneyRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(
                UiUtils.numberRenderer(value -> Integer.toString((int) value)));
        table.getColumnModel().getColumn(7).setCellRenderer(UiUtils.badgeRenderer());
    }

    private void applyFilter() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        StudentStatus status = switch (statusFilter.getSelectedIndex()) {
            case 1 -> StudentStatus.ACTIVE;
            case 2 -> StudentStatus.INACTIVE;
            case 3 -> StudentStatus.GRADUATED;
            default -> null;
        };
        sorter.setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends StudentTableModel, ? extends Integer> entry) {
                Student student = model.get(entry.getIdentifier());
                boolean matchesStatus = status == null || student.status() == status;
                boolean matchesQuery = query.isEmpty()
                        || student.fullName().toLowerCase().contains(query)
                        || student.email().toLowerCase().contains(query)
                        || student.program().toLowerCase().contains(query)
                        || student.id().toLowerCase().contains(query);
                return matchesStatus && matchesQuery;
            }
        });
        updateFooter();
    }

    private void updateFooter() {
        long active = service.count(StudentStatus.ACTIVE);
        footer.setText(table.getRowCount() + " of " + model.getRowCount()
                + " students shown  •  " + active + " active");
    }

    private void updateButtons() {
        boolean selected = table.getSelectedRow() >= 0;
        editButton.setEnabled(selected);
        deleteButton.setEnabled(selected);
    }

    private Student selectedStudent() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        return model.get(table.convertRowIndexToModel(viewRow));
    }

    private void addStudent() {
        if (StudentDialog.showDialog(this, service, null)) {
            refresh();
        }
    }

    private void editSelected() {
        Student student = selectedStudent();
        if (student == null) {
            return;
        }
        if (StudentDialog.showDialog(this, service, student)) {
            refresh();
        }
    }

    private void deleteSelected() {
        Student student = selectedStudent();
        if (student == null) {
            return;
        }
        boolean confirmed = UiUtils.confirm(this, "Delete student",
                "Delete " + student.fullName() + " (" + student.id() + ")?\n"
                        + "This action cannot be undone.");
        if (!confirmed) {
            return;
        }
        try {
            service.delete(student.id());
            refresh();
        } catch (ValidationException ex) {
            UiUtils.error(this, ex.getMessage());
        }
    }

    @Override
    public void refresh() {
        String keepSearch = searchField.getText();
        model.setRows(service.all());
        searchField.setText(keepSearch);
        applyFilter();
        updateButtons();
    }
}
