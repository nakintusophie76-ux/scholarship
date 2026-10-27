package com.scholar.service;

import com.scholar.model.Student;
import com.scholar.model.StudentAccount;
import com.scholar.model.StudentStatus;
import com.scholar.store.DataStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudentAccountServiceTest {

    @TempDir
    Path directory;

    private DataStore store;
    private StudentAccountService accounts;

    @BeforeEach
    void setUp() {
        store = new DataStore(directory);
        store.initialize();
        accounts = new StudentAccountService(store);
    }

    @Test
    void seededDemoStudentCanSignIn() throws Exception {
        StudentAccount account = accounts.login("AMINA", "student123");
        assertEquals("STU-0001", account.studentId());
        assertEquals("Amina Yusuf", store.findStudent(account.studentId()).fullName());
    }

    @Test
    void wrongPasswordAndUnknownStudentAreRejected() {
        assertThrows(ValidationException.class, () -> accounts.login("amina", "wrong"));
        assertThrows(ValidationException.class, () -> accounts.login("unknown", "student123"));
    }

    @Test
    void registrationLinksExistingStudentAndPersists() throws Exception {
        StudentAccount created = accounts.register("STU-0003", "grace.mensah@example.edu",
                "Grace.M", "grace123", "grace123");
        assertEquals("grace.m", created.username());
        assertEquals("STU-0003", created.studentId());
        assertTrue(accounts.hasAccount("STU-0003"));

        DataStore reloaded = new DataStore(directory);
        reloaded.initialize();
        StudentAccountService afterRestart = new StudentAccountService(reloaded);
        assertEquals("STU-0003", afterRestart.login("grace.m", "grace123").studentId());
    }

    @Test
    void registrationRequiresStudentIdAndEmailOnFile() {
        assertThrows(ValidationException.class, () -> accounts.register("STU-9999",
                "grace.mensah@example.edu", "grace.m", "grace123", "grace123"));
        assertThrows(ValidationException.class, () -> accounts.register("STU-0003",
                "wrong@example.edu", "grace.m", "grace123", "grace123"));
        assertFalse(accounts.hasAccount("STU-0003"));
    }

    @Test
    void duplicateStudentAccountAndUsernameAreRejected() throws Exception {
        accounts.register("STU-0003", "grace.mensah@example.edu",
                "grace.m", "grace123", "grace123");

        ValidationException duplicateAccount = assertThrows(ValidationException.class,
                () -> accounts.register("STU-0003", "grace.mensah@example.edu",
                        "grace.second", "grace123", "grace123"));
        assertTrue(duplicateAccount.getMessage().contains("already exists"));

        ValidationException duplicateUsername = assertThrows(ValidationException.class,
                () -> accounts.register("STU-0007", "priya.nair@example.edu",
                        "GRACE.M", "priya123", "priya123"));
        assertTrue(duplicateUsername.getMessage().contains("already in use"));
    }

    @Test
    void invalidUsernameAndPasswordRulesAreRejected() {
        assertThrows(ValidationException.class, () -> accounts.register("STU-0003",
                "grace.mensah@example.edu", "a", "grace123", "grace123"));
        assertThrows(ValidationException.class, () -> accounts.register("STU-0003",
                "grace.mensah@example.edu", "grace.m", "short", "short"));
        assertThrows(ValidationException.class, () -> accounts.register("STU-0003",
                "grace.mensah@example.edu", "grace.m", "grace123", "different"));
    }

    @Test
    void passwordChangePersistsAndInvalidatesOldPassword() throws Exception {
        StudentAccount account = accounts.login("amina", "student123");
        StudentAccount updated = accounts.changePassword(account,
                "student123", "newpass1", "newpass1");
        assertNotNull(updated);

        DataStore reloaded = new DataStore(directory);
        reloaded.initialize();
        StudentAccountService afterRestart = new StudentAccountService(reloaded);
        assertEquals("STU-0001", afterRestart.login("amina", "newpass1").studentId());
        assertThrows(ValidationException.class, () -> afterRestart.login("amina", "student123"));
    }

    @Test
    void deletingStudentAlsoRemovesPortalAccount() throws Exception {
        StudentService students = new StudentService(store);
        Student created = students.save(new Student("", "Portal", "Only",
                "portal.only@example.edu", "+1 202 555 0111", "History", 1, 3.1, 12000,
                StudentStatus.ACTIVE));
        accounts.register(created.id(), created.email(), "portal.only",
                "portal123", "portal123");

        students.delete(created.id());
        assertNull(store.findStudent(created.id()));
        assertNull(store.findStudentAccountByStudentId(created.id()));
    }
}
