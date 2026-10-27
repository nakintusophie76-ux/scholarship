package com.scholar.service;

import com.scholar.model.Student;
import com.scholar.model.StudentStatus;
import com.scholar.store.DataStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudentServiceTest {

    @TempDir
    Path directory;

    private DataStore store;
    private StudentService students;

    @BeforeEach
    void setUp() {
        store = new DataStore(directory);
        store.initialize();
        students = new StudentService(store);
    }

    @Test
    void duplicateEmailIsRejectedCaseInsensitively() {
        Student duplicate = new Student("", "Someone", "Else", "AMINA.YUSUF@example.edu",
                "", "History", 1, 3.0, 10000, StudentStatus.ACTIVE);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> students.save(duplicate));
        assertTrue(ex.getMessage().contains("already uses the email"));
    }

    @Test
    void gpaAndYearAreValidated() {
        Student badGpa = new Student("", "Test", "Student", "test.gpa@example.edu",
                "", "History", 1, 4.5, 10000, StudentStatus.ACTIVE);
        assertThrows(ValidationException.class, () -> students.save(badGpa));

        Student badYear = new Student("", "Test", "Student", "test.year@example.edu",
                "", "History", 8, 3.0, 10000, StudentStatus.ACTIVE);
        assertThrows(ValidationException.class, () -> students.save(badYear));
    }

    @Test
    void newStudentReceivesSequentialIdAndIsPersisted() throws Exception {
        Student saved = students.save(new Student("", "Maya", "Johnson",
                "maya.johnson@example.edu", "+1 202 555 0199", "Architecture", 2,
                3.55, 24000, StudentStatus.ACTIVE));

        assertEquals("STU-0009", saved.id());
        assertNotNull(store.findStudent("STU-0009"));

        DataStore reloaded = new DataStore(directory);
        reloaded.initialize();
        assertEquals("Maya", reloaded.findStudent("STU-0009").firstName());
        assertEquals(9, reloaded.students().size());
    }

    @Test
    void studentWithApplicationsCannotBeDeleted() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> students.delete("STU-0001"));
        assertTrue(ex.getMessage().contains("application(s) reference"));
    }

    @Test
    void studentWithoutApplicationsCanBeDeleted() throws Exception {
        Student saved = students.save(new Student("", "Temporary", "Record",
                "temporary.record@example.edu", "", "History", 1, 2.5, 1000,
                StudentStatus.INACTIVE));
        students.delete(saved.id());
        assertEquals(null, store.findStudent(saved.id()));
    }

    @Test
    void contactDetailsCanBeUpdatedAndPersist() throws Exception {
        Student updated = students.updateContact("STU-0001",
                "amina.new@example.edu", "+1 202 555 0999");
        assertEquals("amina.new@example.edu", updated.email());
        assertEquals("+1 202 555 0999", updated.phone());

        DataStore reloaded = new DataStore(directory);
        reloaded.initialize();
        assertEquals("amina.new@example.edu", reloaded.findStudent("STU-0001").email());
    }

    @Test
    void contactUpdateRejectsInvalidOrDuplicateEmail() {
        assertThrows(ValidationException.class,
                () -> students.updateContact("STU-0001", "not-an-email", ""));
        assertThrows(ValidationException.class,
                () -> students.updateContact("STU-0001", "daniel.okafor@example.edu", ""));
    }
}
