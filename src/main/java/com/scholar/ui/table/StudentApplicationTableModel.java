package com.scholar.ui.table;

import com.scholar.model.Application;
import com.scholar.model.ApplicationStatus;
import com.scholar.model.Scholarship;
import com.scholar.util.Dates;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The signed in student's own applications, with the scholarship name and
 * award value resolved for display.
 */
public final class StudentApplicationTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {
            "ID", "Scholarship", "Award", "Submitted", "Status", "Decision"
    };

    private List<Application> rows = new ArrayList<>();
    private Map<String, Scholarship> scholarships = Map.of();

    public void setRows(List<Application> applications, Map<String, Scholarship> scholarships) {
        this.rows = new ArrayList<>(applications);
        this.scholarships = scholarships;
        fireTableDataChanged();
    }

    public Application get(int modelRow) {
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
            case 2 -> Double.class;
            case 4 -> ApplicationStatus.class;
            default -> String.class;
        };
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Application application = rows.get(rowIndex);
        Scholarship scholarship = scholarships.get(application.scholarshipId());
        return switch (columnIndex) {
            case 0 -> application.id();
            case 1 -> scholarship == null ? application.scholarshipId() : scholarship.name();
            case 2 -> scholarship == null ? 0.0 : scholarship.amount();
            case 3 -> Dates.format(application.submittedAt());
            case 4 -> application.status();
            case 5 -> application.reviewedAt() == null ? "Awaiting review"
                    : Dates.format(application.reviewedAt());
            default -> "";
        };
    }
}
