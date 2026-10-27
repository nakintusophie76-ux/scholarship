package com.scholar.store;

import com.scholar.model.AdminUser;
import com.scholar.model.Application;
import com.scholar.model.ApplicationStatus;
import com.scholar.model.Scholarship;
import com.scholar.model.ScholarshipType;
import com.scholar.model.Student;
import com.scholar.model.StudentAccount;
import com.scholar.model.StudentStatus;
import com.scholar.util.Passwords;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Demo data written the first time the application starts. Dates are relative
 * to "today" so the dashboard always shows open and closed deadlines.
 */
final class SeedData {

    private SeedData() {
    }

    static void populate(List<Student> students,
                         List<Scholarship> scholarships,
                         List<Application> applications,
                         List<AdminUser> users,
                         List<StudentAccount> studentAccounts) {

        LocalDate today = LocalDate.now();

        students.add(new Student("STU-0001", "Amina", "Yusuf", "amina.yusuf@example.edu",
                "+1 202 555 0142", "Computer Science", 3, 3.82, 18500, StudentStatus.ACTIVE));
        students.add(new Student("STU-0002", "Daniel", "Okafor", "daniel.okafor@example.edu",
                "+1 202 555 0187", "Business Administration", 2, 3.21, 42000, StudentStatus.ACTIVE));
        students.add(new Student("STU-0003", "Grace", "Mensah", "grace.mensah@example.edu",
                "+1 202 555 0119", "Medicine", 4, 3.91, 26000, StudentStatus.ACTIVE));
        students.add(new Student("STU-0004", "Liam", "Carter", "liam.carter@example.edu",
                "+1 202 555 0163", "Mechanical Engineering", 1, 2.86, 68000, StudentStatus.ACTIVE));
        students.add(new Student("STU-0005", "Sofia", "Alvarez", "sofia.alvarez@example.edu",
                "+1 202 555 0175", "Data Science", 3, 3.64, 15000, StudentStatus.ACTIVE));
        students.add(new Student("STU-0006", "Noah", "Kim", "noah.kim@example.edu",
                "+1 202 555 0108", "Law", 2, 3.45, 54000, StudentStatus.INACTIVE));
        students.add(new Student("STU-0007", "Priya", "Nair", "priya.nair@example.edu",
                "+1 202 555 0151", "Nursing", 4, 3.72, 22500, StudentStatus.ACTIVE));
        students.add(new Student("STU-0008", "Ethan", "Brown", "ethan.brown@example.edu",
                "+1 202 555 0136", "Economics", 1, 3.05, 91000, StudentStatus.ACTIVE));

        scholarships.add(new Scholarship("SCH-0001", "Future Innovators Merit Award",
                "Horizon Foundation", ScholarshipType.MERIT, 5000, 5, 3.50, 0,
                today.plusDays(21), "For high achieving students in STEM and computing.", true));
        scholarships.add(new Scholarship("SCH-0002", "Bright Start Need Grant",
                "Community Trust", ScholarshipType.NEED, 3500, 8, 2.50, 30000,
                today.plusDays(45), "Need based support for students from lower income households.", true));
        scholarships.add(new Scholarship("SCH-0003", "Champions Sports Scholarship",
                "Victory Athletics", ScholarshipType.SPORTS, 2500, 3, 2.80, 0,
                today.plusDays(10), "Recognises student athletes who represent the university.", true));
        scholarships.add(new Scholarship("SCH-0004", "Research Excellence Fund",
                "Northwind Institute", ScholarshipType.RESEARCH, 6000, 2, 3.60, 0,
                today.minusDays(5), "Funding for final year research projects with published potential.", true));
        scholarships.add(new Scholarship("SCH-0005", "Community Leaders Award",
                "City Rotary Club", ScholarshipType.COMMUNITY, 2000, 4, 3.00, 0,
                today.plusDays(60), "For students leading volunteering and community projects.", true));

        LocalDateTime now = LocalDateTime.now().withNano(0);

        applications.add(new Application("APP-0001", "STU-0001", "SCH-0001",
                now.minusDays(6), ApplicationStatus.PENDING,
                "I am building an assistive technology project for my final year capstone.",
                "", null));
        applications.add(new Application("APP-0002", "STU-0003", "SCH-0004",
                now.minusDays(28), ApplicationStatus.APPROVED,
                "My research explores low cost diagnostic tools for rural clinics.",
                "Strong proposal and excellent academic record. Approved.", now.minusDays(20)));
        applications.add(new Application("APP-0003", "STU-0007", "SCH-0002",
                now.minusDays(15), ApplicationStatus.APPROVED,
                "I work part time while completing my nursing placement.",
                "Household income verified. Award approved.", now.minusDays(9)));
        applications.add(new Application("APP-0004", "STU-0005", "SCH-0001",
                now.minusDays(12), ApplicationStatus.APPROVED,
                "I maintain a 3.6 GPA while tutoring first year programming students.",
                "Meets the merit threshold. Approved.", now.minusDays(4)));
        applications.add(new Application("APP-0005", "STU-0002", "SCH-0005",
                now.minusDays(4), ApplicationStatus.PENDING,
                "I coordinate a weekend financial literacy club for local high schoolers.",
                "", null));
        applications.add(new Application("APP-0006", "STU-0004", "SCH-0003",
                now.minusDays(18), ApplicationStatus.REJECTED,
                "I play for the university football team and coach a junior squad.",
                "The three available places were filled by higher ranked applicants.",
                now.minusDays(11)));
        applications.add(new Application("APP-0007", "STU-0008", "SCH-0003",
                now.minusDays(2), ApplicationStatus.PENDING,
                "I compete in track and field and volunteer as an athletics mentor.",
                "", null));
        applications.add(new Application("APP-0008", "STU-0001", "SCH-0002",
                now.minusDays(8), ApplicationStatus.WAITLISTED,
                "I receive partial support from my department but still have a funding gap.",
                "Placed on the waitlist pending confirmation of remaining funds.",
                now.minusDays(3)));
        applications.add(new Application("APP-0009", "STU-0007", "SCH-0003",
                now.minusDays(22), ApplicationStatus.APPROVED,
                "I represent the university in national netball competitions.",
                "Approved for the sports scholarship.", now.minusDays(16)));
        applications.add(new Application("APP-0010", "STU-0005", "SCH-0005",
                now.minusDays(1), ApplicationStatus.PENDING,
                "I founded a peer mentoring group that supports 40 first year students.",
                "", null));

        users.add(defaultAdmin());

        studentAccounts.add(demoAccount("STU-0001", "amina", "student123"));
        studentAccounts.add(demoAccount("STU-0005", "sofia", "student123"));
    }

    static AdminUser defaultAdmin() {
        String salt = Passwords.newSalt();
        return new AdminUser("admin", "System Administrator", salt,
                Passwords.hash("admin123", salt));
    }

    private static StudentAccount demoAccount(String studentId, String username, String password) {
        String salt = Passwords.newSalt();
        return new StudentAccount(studentId, username, salt, Passwords.hash(password, salt));
    }
}
