# Scholar Manager

A complete desktop **Scholarship Management System** built with **Java 17** and **Swing**. It gives a scholarship office one place to manage students, scholarship programmes, applications, award decisions and CSV reports — and gives students their own self-service portal to create an account, apply for funding and track decisions. There is no database or server to install.

The interface uses a custom indigo/slate theme on top of **FlatLaf**, with a split-screen sign-in page, separate administrator and student workspaces, dashboards, data tables, modal forms and live eligibility checks.

---

## Demo accounts

| Role | Username | Password | Student |
| ---- | -------- | -------- | ------- |
| Administrator | `admin` | `admin123` | — |
| Student | `amina` | `student123` | Amina Yusuf (STU-0001) |
| Student | `sofia` | `student123` | Sofia Alvarez (STU-0005) |

Choose **Administrator** or **Student** on the sign-in screen. Passwords can be changed from **Change password** in the sidebar. They are stored as PBKDF2 hashes, never as plain text.

New students can create an account from the sign-in screen with their **student ID** and the **email address already on file**.

---

## Quick start

### Option 1 — Run the ready-made JAR (fastest)

1. Open the project folder:
   `C:\Users\abuca\OneDrive\Desktop\SCHOLAR MANAGER`
2. Double-click **`run.bat`**.
   The script starts `target\ScholarManager.jar`; if the JAR is missing it tries to build it first.
3. Sign in with `admin` / `admin123` for the office workspace, or choose **Student** and use `amina` / `student123`.

Or from a terminal in the project folder:

```bat
java -jar target\ScholarManager.jar
```

Requires **Java 17 or newer** on `PATH`.

### Option 2 — Open and run in NetBeans

1. Start NetBeans.
2. Choose **File → Open Project…**
3. Select the folder `SCHOLAR MANAGER` (the folder containing `pom.xml`).
4. Wait for NetBeans to finish downloading Maven dependencies (first run only).
5. Press **F6** (Run Project).

The project is a standard Maven project, so NetBeans recognises it without extra setup. The main class (`com.scholar.App`) is already configured in `pom.xml`.

### Option 3 — Build from the command line

If Maven is on `PATH`:

```bat
mvn clean package
java -jar target\ScholarManager.jar
```

With the Maven bundled inside NetBeans:

```bat
"C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd" clean package
```

The shaded, runnable output is always **`target\ScholarManager.jar`**.

---

## What the application does

### 1. Sign in

- Split-screen branded login with an **Administrator / Student** role switch.
- Separate credential checks for office staff and student portal accounts.
- Username/password validation with inline error messages.
- Show/hide password toggle.
- Role-specific demo-account hints.
- Students can create an account with their student ID and email address on file.
- Sign out returns to the login screen.

### 2. Student portal

Students get a dedicated workspace with the same visual language as the office app:

- **Dashboard** — open scholarships, application counts, pending decisions, total funding awarded, recommended programmes and the latest decisions.
- **Browse scholarships** — searchable catalogue with award, minimum GPA, income limit, deadline and a live **Eligible / Applied / Not eligible** badge. Eligible programmes can be applied for directly.
- **My applications** — every submission with its status, decision date and full office review notes.
- **My profile** — view the academic record, update contact email and phone, and review portal account details.
- **Change password** and sign out from the sidebar.

Students can only change contact details and their own password. Academic, household and enrollment fields stay under office control.

### 3. Administrator dashboard

- KPI cards: students, open scholarships, pending reviews, total awarded.
- Applications-by-status horizontal bar chart drawn with Java2D.
- Upcoming deadlines with urgency badges.
- Five most recent applications.
- Everything refreshes when the page is opened or **F5** is pressed.

### 4. Students

- Searchable, sortable student directory.
- Filter by status: active, inactive, graduated.
- Add and edit students in a modal form with validation.
- Delete protection: a student referenced by an application cannot be deleted.
- Fields: name, email, phone, programme, year, GPA, household income, status.

### 5. Scholarships

- Manage scholarship programmes and sponsors.
- Five scholarship types: merit, need, sports, research, community.
- Award amount, number of places, minimum GPA, optional household-income limit, deadline, active/archived flag.
- Live occupancy indicator showing places used and remaining.
- Delete protection: a scholarship referenced by an application cannot be deleted; archive it instead.

### 6. Applications and awarding

- Search by application ID, student or scholarship.
- Filter by status.
- Submit a new application with a live eligibility checklist.
- Student submissions arrive here for review.
- **Review / award** dialog for **Approve**, **Waitlist** or **Reject**, including review notes. Approving an application is the award decision that is shown to the student.
- Capacity protection when approving.
- Delete an application when it is no longer needed.

### 7. Reports

- Live award summary per scholarship: places awarded, places remaining, total committed.
- One-click CSV exports for:
  - students,
  - applications,
  - approved awards.
- Export destination is chosen with a file chooser; `.csv` is added automatically.
- "Open data folder" shortcut.
- CSV files are UTF-8, properly escaped and include headers.

---

## Business rules

An application can only be submitted when **all** of these are true:

- The student is **active**.
- The scholarship is **active** (not archived).
- The deadline has **not passed**.
- The student's GPA is at least the scholarship's **minimum GPA**.
- If the scholarship has an income limit, **household income ≤ income limit**.
- At least **one place remains**.
- The student does **not already have an application** for the same scholarship.

Approving an application re-checks capacity, so two reviewers cannot overfill a programme. Moving an approved application to **Rejected** or **Waitlisted** frees the place, and the next eligible applicant can then be approved.

Other safeguards:

- Student emails must be valid and unique (case-insensitive).
- Scholarship names must be unique (case-insensitive).
- Year must be 1–6, GPA 0.00–4.00, income ≥ 0, award amount > 0, places ≥ 1.
- Referenced students and scholarships cannot be deleted.
- All data changes are written to disk immediately.

### Student portal accounts

- Registration requires the **student ID** and the **email address already on the student record**.
- Each student record can have at most one portal account.
- Usernames are lowercase, 3–20 characters, limited to letters, numbers, dots, dashes and underscores, and must not collide with an administrator or another student.
- Passwords need at least 6 characters and are stored as salted PBKDF2 hashes.
- Students can update only their contact email and phone; academic and household data remain office-controlled.
- Deleting a student also removes the linked portal account.

---

## Project structure

```text
SCHOLAR MANAGER
├── pom.xml                     Maven build, FlatLaf, JUnit, shaded JAR
├── run.bat                     Build-if-needed + launch script
├── README.md
├── data/                       Created on first run (CSV data)
│   ├── students.csv
│   ├── scholarships.csv
│   ├── applications.csv
│   ├── users.csv
│   └── student_accounts.csv
├── target/
│   └── ScholarManager.jar      Runnable application
└── src
    ├── main/java/com/scholar
    │   ├── App.java            Entry point
    │   ├── model/              Records and enums
    │   ├── store/              CSV persistence, seed data, CSV parser
    │   ├── service/            Validation, auth, eligibility, reports
    │   ├── ui/                 Frames, panels, dialogs, theme, icons
    │   ├── ui/table/           Swing table models
    │   └── util/               Dates, IDs, money, password hashing
    └── test/java/com/scholar/service
        ├── ApplicationServiceTest.java
        ├── AuthAndReportServiceTest.java
        ├── StudentAccountServiceTest.java
        └── StudentServiceTest.java
```

### Architecture

```text
UI (Swing) → Service (rules) → DataStore (CSV files) → data/*.csv
```

- **Model** — immutable records (`Student`, `Scholarship`, `Application`, `AdminUser`, `StudentAccount`) and enums.
- **Store** — loads collections into memory and flushes each change to CSV with UTF-8 and safe escaping.
- **Service** — all business rules; the UI never decides eligibility or capacity itself. `StudentAccountService` handles portal registration, sign-in and password changes.
- **UI** — reusable themed components (`RoundedPanel`, `StatCard`, `UiUtils`) and table models, shared by the administrator and student workspaces.

---

## Demo data

On a clean start the app creates:

- **8 students**
- **5 scholarships**
- **10 applications**: 4 approved, 4 pending, 1 waitlisted, 1 rejected
- **$17,000** in committed awards
- **2 student portal accounts**: `amina` (STU-0001) and `sofia` (STU-0005), both with password `student123`

Deadlines are calculated relative to the day the demo data is created, so the dashboard shows both open and closed programmes.

---

## Data storage and reset

All data lives in plain CSV files under `data/` beside the project/working folder:

| File | Contents |
| ---- | -------- |
| `students.csv` | Student records |
| `scholarships.csv` | Scholarship programmes |
| `applications.csv` | Applications and review decisions |
| `users.csv` | Administrator accounts (PBKDF2 hashes) |
| `student_accounts.csv` | Student portal logins linked to student IDs (PBKDF2 hashes) |

- The folder is created automatically on first run.
- Writes are immediate; there is no separate "Save" step.
- Back up the folder by copying it.
- **Reset the app:** close it and delete the `data` folder. The next launch recreates the demo data and the default account.
- **Use a different data folder:**

```bat
java -Dscholar.data.dir="C:\path\to\scholar-data" -jar target\ScholarManager.jar
```

The active data folder is printed in the console at startup.

> `data/` is runtime state, not source code. It is listed in `.gitignore`.

---

## Tests

The project has **30 automated tests** covering the service layer:

| Test class | Tests | Coverage |
| ---------- | ----- | -------- |
| `ApplicationServiceTest` | 9 | seeded totals, duplicate applications, each eligibility reason, persistence, capacity, freed capacity, CSV escaping, status counts, per-student portal queries |
| `AuthAndReportServiceTest` | 6 | login, wrong credentials, password change persistence, confirmation rules, award summary, all CSV exports |
| `StudentAccountServiceTest` | 8 | seeded student sign-in, registration and persistence, student/email verification, duplicate accounts and usernames, username/password rules, password changes, account cascade deletion |
| `StudentServiceTest` | 7 | unique email, GPA/year validation, ID generation and persistence, deletion guards, contact updates |

Run them with:

```bat
mvn test
```

or, using NetBeans Maven:

```bat
"C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd" test
```

Every test creates its data in an isolated JUnit temporary directory, so the demo data is never modified.

---

## Requirements

- **JDK 17 or newer** (the project targets Java 17 and also runs on newer JDKs).
- **Maven 3.6+** for building — or just use NetBeans, which bundles Maven.
- Windows, macOS or Linux with a graphical desktop (Swing).
- Internet access on the first Maven build to download FlatLaf and JUnit.

No database, application server, browser or additional runtime is required.

---

## Troubleshooting

| Problem | Fix |
| ------- | --- |
| `mvn` is not recognised | Use NetBeans **Run Project**, or the bundled Maven path shown above. |
| `target\ScholarManager.jar` is locked during a rebuild | Close the running Scholar Manager window, then build again. |
| Data folder cannot be written | Launch with `-Dscholar.data.dir="C:\writable\folder"` or move the project out of a read-only/synced location. |
| Login no longer works after experimenting | Close the app, delete the `data` folder, relaunch and use `admin` / `admin123` or a student demo login. |
| Student registration says details do not match | The student ID and email must match an existing student record. Ask the office to check the record. |
| FlatLaf warnings about "restricted method" / native access | Harmless JVM notices on newer JDKs; they do not affect the app. |
| UI scaling looks large/small | Use the Windows display-scaling setting; FlatLaf follows the system scale. |

---

## Design notes

- **FlatLaf 3.4.1** provides the modern component baseline; the sidebar, cards, badges and chart are custom-painted.
- **No external database** — CSV keeps the project completely self-contained and easy to inspect.
- **PBKDF2WithHmacSHA256** with a random salt protects administrator and student portal passwords.
- **Layered services** keep every rule testable without launching Swing.
- **Shared theme components** keep the office workspace and student portal visually consistent.
- **`release 17`** compilation keeps the JAR compatible with Java 17+ while the build itself can run on a newer JDK.

---

## Verification checklist

- [x] Maven project opens and builds in NetBeans.
- [x] `mvn clean package` succeeds and produces the shaded runnable JAR.
- [x] All 30 service tests pass.
- [x] Login screen launches with the seeded administrator and student accounts.
- [x] Student registration, sign-in and portal navigation work.
- [x] All five administrator pages construct and refresh without errors (Dashboard, Students, Scholarships, Applications, Reports).
- [x] All four student pages construct and refresh without errors (Dashboard, Browse scholarships, My applications, My profile).
- [x] Student applications appear in the administrator queue and decisions appear in the portal.
- [x] Sidebar navigation and **F5** refresh work.
- [x] Student and scholarship CRUD is validated and persisted.
- [x] Eligibility, duplicate, capacity and deadline rules are enforced.
- [x] CSV exports produce the expected rows.
- [x] Password change persists across restarts and invalidates the old password.
