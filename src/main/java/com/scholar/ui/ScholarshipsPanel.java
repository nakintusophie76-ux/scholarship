package com.scholar.ui;

import com.scholar.model.Scholarship;
import com.scholar.service.ApplicationService;
import com.scholar.service.ScholarshipService;
import com.scholar.service.ValidationException;
import com.scholar.ui.table.ScholarshipTableModel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SortOrder;
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

/**
 * Scholarship catalogue with capacity, deadline and status information.
 */
public final class ScholarshipsPanel extends JPanel implements Refreshable {

    private final ScholarshipService service;
    private final ApplicationService applications;
    private final ScholarshipTableModel model = new ScholarshipTableModel();
    private final JTable table = new JTable(model);
    private final TableRowSorter<ScholarshipTableModel> sorter = new TableRowSorter<>(model);
    private final JTextField searchField = UiUtils.searchField("Search scholarship or sponsor…");
    private final JComboBox<String> statusFilter =
            UiUtils.comboBox(new String[]{"All programmes", "Open", "Closed"});
    private final JLabel footer = UiUtils.muted("Loading…");
    private final JButton editButton = UiUtils.secondaryButton("Edit");
    private final JButton deleteButton = UiUtils.dangerButton("Delete");

    public ScholarshipsPanel(ScholarshipService service, ApplicationService applications) {
        this.service = service;
        this.applications = applications;
        setOpaque(false);
        setLayout(new BorderLayout(0, 14));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        table.setRowSorter(sorter);
        configureColumns();
        sorter.setSortKeys(List.of(new TableRowSorter.SortKey(6, SortOrder.ASCENDING)));

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
        JButton add = UiUtils.primaryButton("+ New scholarship");
        add.addActionListener(e -> addScholarship());
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
        panel.add(UiUtils.rightAligned("Places shows awarded / total"), BorderLayout.EAST);
        return panel;
    }

    private void configureColumns() {
        int[] widths = {90, 250, 90, 100, 80, 80, 120, 80};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.getColumnModel().getColumn(3).setCellRenderer(UiUtils.moneyRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(UiUtils.gpaRenderer());
        table.getColumnModel().getColumn(7).setCellRenderer(UiUtils.badgeRenderer());
    }

    private void applyFilter() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        Boolean openOnly = switch (statusFilter.getSelectedIndex()) {
            case 1 -> Boolean.TRUE;
            case 2 -> Boolean.FALSE;
            default -> null;
        };
        LocalDate today = LocalDate.now();
        sorter.setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends ScholarshipTableModel, ? extends Integer> entry) {
                Scholarship scholarship = model.get(entry.getIdentifier());
                boolean matchesStatus = openOnly == null || scholarship.isOpen(today) == openOnly;
                boolean matchesQuery = query.isEmpty()
                        || scholarship.name().toLowerCase().contains(query)
                        || scholarship.sponsor().toLowerCase().contains(query)
                        || scholarship.type().label().toLowerCase().contains(query)
                        || scholarship.id().toLowerCase().contains(query);
                return matchesStatus && matchesQuery;
            }
        });
        updateFooter();
    }

    private void updateFooter() {
        long open = service.countOpen();
        long places = service.all().stream().mapToLong(Scholarship::slots).sum();
        footer.setText(table.getRowCount() + " of " + model.getRowCount()
                + " programmes shown  •  " + open + " open  •  " + places + " total places");
    }

    private void updateButtons() {
        boolean selected = table.getSelectedRow() >= 0;
        editButton.setEnabled(selected);
        deleteButton.setEnabled(selected);
    }

    private Scholarship selectedScholarship() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        return model.get(table.convertRowIndexToModel(viewRow));
    }

    private void addScholarship() {
        if (ScholarshipDialog.showDialog(this, service, null)) {
            refresh();
        }
    }

    private void editSelected() {
        Scholarship scholarship = selectedScholarship();
        if (scholarship == null) {
            return;
        }
        if (ScholarshipDialog.showDialog(this, service, scholarship)) {
            refresh();
        }
    }

    private void deleteSelected() {
        Scholarship scholarship = selectedScholarship();
        if (scholarship == null) {
            return;
        }
        boolean confirmed = UiUtils.confirm(this, "Delete scholarship",
                "Delete \"" + scholarship.name() + "\"?\nThis action cannot be undone.");
        if (!confirmed) {
            return;
        }
        try {
            service.delete(scholarship.id());
            refresh();
        } catch (ValidationException ex) {
            UiUtils.error(this, ex.getMessage());
        }
    }

    @Override
    public void refresh() {
        List<Scholarship> all = service.all();
        Map<String, Integer> approved = new HashMap<>();
        for (Scholarship scholarship : all) {
            approved.put(scholarship.id(), (int) applications.approvedCount(scholarship.id()));
        }
        model.setRows(all, approved);
        applyFilter();
        updateButtons();
    }
}
