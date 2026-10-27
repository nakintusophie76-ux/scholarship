package com.scholar.ui.table;

import com.scholar.model.Student;
import com.scholar.model.StudentStatus;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Read-only table model for student records.
 */
public final class StudentTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {
            "ID", "Student", "Email", "Programme", "Year", "GPA", "Household income", "Status"
    };

    private List<Student> rows = new ArrayList<>();

    public void setRows(List<Student> students) {
        this.rows = new ArrayList<>(students);
        fireTableDataChanged();
    }

    public Student get(int modelRow) {
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
            case 4 -> Integer.class;
            case 5, 6 -> Double.class;
            case 7 -> StudentStatus.class;
            default -> String.class;
        };
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Student student = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> student.id();
            case 1 -> student.fullName();
            case 2 -> student.email();
            case 3 -> student.program();
            case 4 -> student.year();
            case 5 -> student.gpa();
            case 6 -> student.householdIncome();
            case 7 -> student.status();
            default -> "";
        };
    }
}
