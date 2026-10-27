package com.scholar.ui.table;

import com.scholar.service.ReportService;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Table model for the "awards by scholarship" report.
 */
public final class ScholarshipReportTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {
            "Scholarship", "Type", "Places", "Awarded", "Remaining", "Total awarded"
    };

    private List<ReportService.ScholarshipReport> rows = new ArrayList<>();

    public void setRows(List<ReportService.ScholarshipReport> reports) {
        this.rows = new ArrayList<>(reports);
        fireTableDataChanged();
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
        return column == 5 ? Double.class : String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ReportService.ScholarshipReport report = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> report.scholarship().name();
            case 1 -> report.scholarship().type().label();
            case 2 -> Integer.toString(report.scholarship().slots());
            case 3 -> Integer.toString(report.approved());
            case 4 -> Integer.toString(report.remaining());
            case 5 -> report.awarded();
            default -> "";
        };
    }
}
