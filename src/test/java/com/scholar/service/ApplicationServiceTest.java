package com.scholar.service;

import com.scholar.model.Application;
import com.scholar.model.ApplicationStatus;
import com.scholar.model.Scholarship;
import com.scholar.model.ScholarshipType;
import com.scholar.store.DataStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationServiceTest {

    @TempDir
    Path directory;

    private DataStore store;
    private ApplicationService applications;
    private ScholarshipService scholarships;

    @BeforeEach
    void setUp() {
        store = new DataStore(directory);
        store.initialize();
        applications = new ApplicationService(store);
        scholarships = new ScholarshipService(store);
    }

    @Test
    void seededTotalsAreConsistent() {
        assertEquals(4, applications.statusCounts().get(ApplicationStatus.APPROVED));
        assertEquals(4, applications.statusCounts().get(ApplicationStatus.PENDING));
        assertEquals(1, applications.statusCounts().get(ApplicationStatus.WAITLISTED));
        assertEquals(1, applications.statusCounts().get(ApplicationStatus.REJECTED));
        assertEquals(17000.0, applications.totalAwarded(), 0.001);
        assertEquals(4, scholarships.countOpen());
    }

    @Test
    void duplicateApplicationIsRejected() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> applications.apply("STU-0001", "SCH-0001", ""));
        assertTrue(ex.getMessage().contains("already has application APP-0001"));
    }

    @Test
    void ineligibleStudentsAreBlockedForTheRightReason() {
        ValidationException lowGpa = assertThrows(ValidationException.class,
                () -> applications.apply("STU-0004", "SCH-0001", ""));
        assertTrue(lowGpa.getMessage().contains("below the minimum"));

        ValidationException income = assertThrows(ValidationException.class,
                () -> applications.apply("STU-0008", "SCH-0002", ""));
        assertTrue(income.getMessage().contains("above the"));

        ValidationException closed = assertThrows(ValidationException.class,
                () -> applications.apply("STU-0003", "SCH-0004", ""));
        assertTrue(closed.getMessage().contains("deadline passed"));

        ValidationException inactive = assertThrows(ValidationException.class,
                () -> applications.apply("STU-0006", "SCH-0005", ""));
        assertTrue(inactive.getMessage().contains("only active students"));
    }

    @Test
    void eligibleApplicationIsSavedAndReloadedFromDisk() throws Exception {
        Application application = applications.apply("STU-0003", "SCH-0003",
                "I represented the university at national level.");
        assertEquals("APP-0011", application.id());
        assertEquals(ApplicationStatus.PENDING, application.status());

        DataStore reloaded = new DataStore(directory);
        reloaded.initialize();
        Application fromDisk = reloaded.findApplication("APP-0011");
        assertNotNull(fromDisk);
        assertEquals("STU-0003", fromDisk.studentId());
        assertEquals("I represented the university at national level.", fromDisk.statement());
    }

    @Test
    void approvingBeyondCapacityIsBlocked() throws Exception {
        Scholarship onePlace = scholarships.save(new Scholarship("", "One Place Test",
                "Test Sponsor", ScholarshipType.MERIT, 1000, 1, 0, 0,
                LocalDate.now().plusDays(30), "Single place for a capacity test.", true));

        Application first = applications.apply("STU-0002", onePlace.id(), "First");
        Application second = applications.apply("STU-0008", onePlace.id(), "Second");

        applications.review(first.id(), ApplicationStatus.APPROVED, "Approved");
        ValidationException ex = assertThrows(ValidationException.class,
                () -> applications.review(second.id(), ApplicationStatus.APPROVED, "Second"));
        assertTrue(ex.getMessage().contains("already awarded"));

        Application waitlisted = applications.review(second.id(),
                ApplicationStatus.WAITLISTED, "Next in line");
        assertEquals(ApplicationStatus.WAITLISTED, waitlisted.status());
        assertEquals(1, applications.approvedCount(onePlace.id()));
    }

    @Test
    void rejectedApplicationFreesCapacity() throws Exception {
        Scholarship onePlace = scholarships.save(new Scholarship("", "Reusable Place Test",
                "Test Sponsor", ScholarshipType.NEED, 2000, 1, 0, 50000,
                LocalDate.now().plusDays(30), "", true));

        Application first = applications.apply("STU-0002", onePlace.id(), "");
        Application second = applications.apply("STU-0007", onePlace.id(), "");
        applications.review(first.id(), ApplicationStatus.APPROVED, "");
        assertEquals(1, applications.approvedCount(onePlace.id()));

        applications.review(first.id(), ApplicationStatus.REJECTED, "Withdrawn");
        assertEquals(0, applications.approvedCount(onePlace.id()));

        applications.review(second.id(), ApplicationStatus.APPROVED, "");
        assertEquals(1, applications.approvedCount(onePlace.id()));
    }

    @Test
    void csvEscapingSurvivesQuotesAndCommas() throws Exception {
        String statement = "I said \"hello\", then explained my goal.";
        Application application = applications.apply("STU-0005", "SCH-0003", statement);

        DataStore reloaded = new DataStore(directory);
        reloaded.initialize();
        assertEquals(statement, reloaded.findApplication(application.id()).statement());
    }

    @Test
    void statusCountsAreComplete() {
        Map<ApplicationStatus, Long> counts = applications.statusCounts();
        assertEquals(ApplicationStatus.values().length, counts.size());
        assertEquals(10, counts.values().stream().mapToLong(Long::longValue).sum());
    }

    @Test
    void studentPortalQueriesAreScopedToTheSignedInStudent() {
        assertEquals(2, applications.applicationsForStudent("STU-0005").size());
        assertTrue(applications.applicationsForStudent("STU-0005").stream()
                .allMatch(a -> a.studentId().equals("STU-0005")));
        assertEquals(5000.0, applications.studentTotalAwarded("STU-0005"), 0.001);
        assertEquals(6000.0, applications.studentTotalAwarded("STU-0007"), 0.001);
        assertEquals(0.0, applications.studentTotalAwarded("STU-0004"), 0.001);
    }
}
