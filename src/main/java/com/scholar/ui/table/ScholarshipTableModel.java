package com.scholar.ui.table;

import com.scholar.model.Scholarship;
import com.scholar.util.Dates;

import javax.swing.table.AbstractTableModel;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Table model for scholarship programmes, including the number of places
 * already awarded.
 */
public final class ScholarshipTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {
            "ID", "Scholarship", "Type", "Award", "Places", "Min GPA", "Deadline", "Status"
    };

    private List<Scholarship> rows = new ArrayList<>();
    private Map<String, Integer> approved = Map.of();

    public void setRows(List<Scholarship> scholarships, Map<String, Integer> approvedCounts) {
        this.rows = new ArrayList<>(scholarships);
        this.approved = approvedCounts;
        fireTableDataChanged();
    }

    public Scholarship get(int modelRow) {
        return rows.get(modelRow);
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return switch (column) {
            case 3, 5 -> Double.class;
            default -> String.class;
        };
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Scholarship scholarship = rows.get(rowIndex);
        int awarded = approved.getOrDefault(scholarship.id(), 0);
        return switch (columnIndex) {
            case 0 -> scholarship.id();
            case 1 -> scholarship.name();
            case 2 -> scholarship.type().label();
            case 3 -> scholarship.amount();
            case 4 -> awarded + " / " + scholarship.slots();
            case 5 -> scholarship.minGpa();
            case 6 -> Dates.format(scholarship.deadline());
            case 7 -> scholarship.isOpen(LocalDate.now()) ? "Open" : "Closed";
            default -> "";
        };
    }
}
