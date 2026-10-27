package com.scholar.service;

import com.scholar.model.AdminUser;
import com.scholar.store.DataStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthAndReportServiceTest {

    @TempDir
    Path directory;

    private DataStore store;
    private AuthService auth;
    private ReportService reports;

    @BeforeEach
    void setUp() {
        store = new DataStore(directory);
        store.initialize();
        auth = new AuthService(store);
        reports = new ReportService(store, new ApplicationService(store));
    }

    @Test
    void defaultAdminCanSignIn() throws Exception {
        AdminUser user = auth.login("admin", "admin123");
        assertEquals("System Administrator", user.displayName());
    }

    @Test
    void wrongPasswordIsRejected() {
        assertThrows(ValidationException.class, () -> auth.login("admin", "wrong"));
        assertThrows(ValidationException.class, () -> auth.login("unknown", "admin123"));
    }

    @Test
    void passwordChangePersistsAndInvalidatesOldPassword() throws Exception {
        AdminUser user = auth.login("admin", "admin123");
        AdminUser updated = auth.changePassword(user, "admin123", "newpass1", "newpass1");
        assertNotEquals(user.passwordHash(), updated.passwordHash());

        DataStore reloaded = new DataStore(directory);
        reloaded.initialize();
        AuthService authAfterRestart = new AuthService(reloaded);
        assertEquals("admin", authAfterRestart.login("admin", "newpass1").username());
        assertThrows(ValidationException.class,
                () -> authAfterRestart.login("admin", "admin123"));
    }

    @Test
    void passwordChangeValidatesConfirmation() throws Exception {
        AdminUser user = auth.login("admin", "admin123");
        assertThrows(ValidationException.class,
                () -> auth.changePassword(user, "admin123", "newpass1", "newpass2"));
        assertThrows(ValidationException.class,
                () -> auth.changePassword(user, "admin123", "short", "short"));
    }

    @Test
    void reportsSummarizeApprovedAwards() {
        List<ReportService.ScholarshipReport> summary = reports.awardsByScholarship();
        assertEquals(5, summary.size());
        double awarded = summary.stream().mapToDouble(ReportService.ScholarshipReport::awarded).sum();
        assertEquals(17000.0, awarded, 0.001);

        ReportService.ScholarshipReport futureInnovators = summary.stream()
                .filter(r -> r.scholarship().id().equals("SCH-0001"))
                .findFirst().orElseThrow();
        assertEquals(1, futureInnovators.approved());
        assertEquals(4, futureInnovators.remaining());
        assertEquals(5000.0, futureInnovators.awarded(), 0.001);
    }

    @Test
    void csvExportsContainExpectedRows() throws Exception {
        Path students = directory.resolve("students-export.csv");
        Path applications = directory.resolve("applications-export.csv");
        Path awards = directory.resolve("awards-export.csv");

        assertEquals(8, reports.exportStudents(students));
        assertEquals(10, reports.exportApplications(applications));
        assertEquals(4, reports.exportAwards(awards));

        assertTrue(Files.readString(students).contains("STU-0001,Amina,Yusuf"));
        assertTrue(Files.readString(applications).contains("Future Innovators Merit Award"));
        assertTrue(Files.readString(awards).contains("6000.00"));
        assertEquals(9, Files.readAllLines(students).size());
        assertEquals(5, Files.readAllLines(awards).size());
    }
}
