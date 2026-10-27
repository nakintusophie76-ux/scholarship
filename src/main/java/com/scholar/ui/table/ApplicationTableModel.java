package com.scholar.ui.table;

import com.scholar.model.Application;
import com.scholar.model.ApplicationStatus;
import com.scholar.model.Scholarship;
import com.scholar.model.Student;
import com.scholar.util.Dates;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Table model for applications. Names are resolved from the supplied maps so
 * the UI never shows raw identifiers in the main columns.
 */
public final class ApplicationTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {
            "ID", "Student", "Scholarship", "Submitted", "Status"
    };

    private List<Application> rows = new ArrayList<>();
    private Map<String, Student> students = Map.of();
    private Map<String, Scholarship> scholarships = Map.of();

    public void setRows(List<Application> applications,
                        Map<String, Student> students,
                        Map<String, Scholarship> scholarships) {
        this.rows = new ArrayList<>(applications);
        this.students = students;
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
        return column == 4 ? ApplicationStatus.class : String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Application application = rows.get(rowIndex);
        Student student = students.get(application.studentId());
        Scholarship scholarship = scholarships.get(application.scholarshipId());
        return switch (columnIndex) {
            case 0 -> application.id();
            case 1 -> student == null ? application.studentId() : student.fullName();
            case 2 -> scholarship == null ? application.scholarshipId() : scholarship.name();
            case 3 -> Dates.format(application.submittedAt());
            case 4 -> application.status();
            default -> "";
        };
    }
}
