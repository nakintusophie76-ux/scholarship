package com.scholar.ui.table;

import com.scholar.model.Scholarship;
import com.scholar.util.Dates;
import com.scholar.util.Money;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Read-only catalogue used by the student portal. The eligibility column is
 * calculated by the panel, because it depends on the signed in student.
 */
public final class StudentScholarshipTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {
            "ID", "Scholarship", "Type", "Award", "Min GPA", "Income limit", "Deadline",
            "Eligibility"
    };

    private List<Scholarship> rows = new ArrayList<>();
    private Map<String, Integer> approved = Map.of();
    private Map<String, String> eligibility = Map.of();

    public void setRows(List<Scholarship> scholarships,
                        Map<String, Integer> approvedCounts,
                        Map<String, String> eligibilityByScholarship) {
        this.rows = new ArrayList<>(scholarships);
        this.approved = approvedCounts;
        this.eligibility = eligibilityByScholarship;
        fireTableDataChanged();
    }

    public Scholarship get(int modelRow) {
        return rows.get(modelRow);
    }

    public String eligibilityOf(Scholarship scholarship) {
        return eligibility.getOrDefault(scholarship.id(), "");
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
            case 3, 4 -> Double.class;
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
            case 4 -> scholarship.minGpa();
            case 5 -> scholarship.hasIncomeLimit()
                    ? Money.format(scholarship.maxIncome()) : "No limit";
            case 6 -> Dates.format(scholarship.deadline())
                    + (awarded > 0 ? "  (" + (scholarship.slots() - awarded) + " left)" : "");
            case 7 -> eligibilityOf(scholarship);
            default -> "";
        };
    }
}
