package com.scholar.ui;

import com.scholar.model.AdminUser;
import com.scholar.service.ApplicationService;
import com.scholar.service.AuthService;
import com.scholar.service.ReportService;
import com.scholar.service.ScholarshipService;
import com.scholar.service.StudentService;
import com.scholar.store.DataStore;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Main workspace: dark sidebar, page header and the five functional pages.
 */
public final class MainFrame extends JFrame {

    private static final DateTimeFormatter HEADER_DATE =
            DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH);

    private record NavItem(String key, String title, String subtitle, ImageIcon icon) {
    }

    private final DataStore store;
    private final AuthService authService;
    private AdminUser currentUser;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel pagesPanel = new JPanel(cardLayout);
    private final Map<String, JComponent> pages = new LinkedHashMap<>();
    private final Map<String, NavButton> navButtons = new LinkedHashMap<>();
    private final List<NavItem> navItems;

    private final JLabel titleLabel = UiUtils.label("Dashboard", Font.BOLD, 22, Theme.INK);
    private final JLabel subtitleLabel = UiUtils.label("",
            Font.PLAIN, 12, Theme.MUTED);
    private String currentKey = "dashboard";

    public MainFrame(DataStore store, AdminUser user) {
        super("Scholar Manager");
        this.store = store;
        this.currentUser = user;
        this.authService = new AuthService(store);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setIconImage(Icons.appIcon());
        setMinimumSize(new Dimension(1100, 700));
        setSize(1320, 840);
        setLocationRelativeTo(null);

        StudentService studentService = new StudentService(store);
        ScholarshipService scholarshipService = new ScholarshipService(store);
        ApplicationService applicationService = new ApplicationService(store);
        ReportService reportService = new ReportService(store, applicationService);

        DashboardPanel dashboard = new DashboardPanel(studentService, scholarshipService,
                applicationService);
        StudentsPanel students = new StudentsPanel(studentService, applicationService);
        ScholarshipsPanel scholarships = new ScholarshipsPanel(scholarshipService,
                applicationService);
        ApplicationsPanel applications = new ApplicationsPanel(applicationService,
                studentService, scholarshipService);
        ReportsPanel reports = new ReportsPanel(reportService, studentService,
                scholarshipService, applicationService, store);

        navItems = List.of(
                new NavItem("dashboard", "Dashboard",
                        "Overview of students, scholarships and awards",
                        Icons.dashboard(18, Theme.SIDEBAR_TEXT)),
                new NavItem("students", "Students",
                        "Manage student records and eligibility data",
                        Icons.students(18, Theme.SIDEBAR_TEXT)),
                new NavItem("scholarships", "Scholarships",
                        "Create and maintain scholarship programmes",
                        Icons.scholarship(18, Theme.SIDEBAR_TEXT)),
                new NavItem("applications", "Applications",
                        "Review submissions and record decisions",
                        Icons.applications(18, Theme.SIDEBAR_TEXT)),
                new NavItem("reports", "Reports",
                        "Award summaries and CSV exports",
                        Icons.reports(18, Theme.SIDEBAR_TEXT)));

        registerPage("dashboard", dashboard);
        registerPage("students", students);
        registerPage("scholarships", scholarships);
        registerPage("applications", applications);
        registerPage("reports", reports);

        pagesPanel.setOpaque(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildMainArea(), BorderLayout.CENTER);
        setContentPane(root);

        installShortcuts();
        showPage("dashboard");
    }

    private void registerPage(String key, JComponent page) {
        pages.put(key, page);
        pagesPanel.add(page, key);
    }

    private JPanel buildMainArea() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Theme.BG);
        main.add(buildHeader(), BorderLayout.NORTH);
        main.add(pagesPanel, BorderLayout.CENTER);
        return main;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setBackground(Theme.CARD);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(16, 28, 15, 28)));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        subtitleLabel.setAlignmentX(LEFT_ALIGNMENT);
        text.add(titleLabel);
        text.add(Box.createVerticalStrut(3));
        text.add(subtitleLabel);
        header.add(text, BorderLayout.WEST);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        JLabel date = UiUtils.label(HEADER_DATE.format(LocalDate.now()),
                Font.BOLD, 12, Theme.TEXT);
        date.setAlignmentX(RIGHT_ALIGNMENT);
        JLabel office = UiUtils.label("Scholarship Office", Font.PLAIN, 11, Theme.MUTED);
        office.setAlignmentX(RIGHT_ALIGNMENT);
        right.add(date);
        right.add(Box.createVerticalStrut(2));
        right.add(office);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(Theme.SIDEBAR);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(248, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 16, 18, 16));

        sidebar.add(buildBrand());
        sidebar.add(sectionLabel("WORKSPACE"));

        for (NavItem item : navItems) {
            NavButton button = new NavButton(item.title(), item.icon());
            button.addActionListener(e -> showPage(item.key()));
            navButtons.put(item.key(), button);
            sidebar.add(button);
            sidebar.add(Box.createVerticalStrut(3));
        }

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(sectionLabel("ACCOUNT"));
        sidebar.add(buildAccountCard());
        sidebar.add(Box.createVerticalStrut(10));

        NavButton changePassword = new NavButton("Change password", Icons.key(17, Theme.SIDEBAR_TEXT));
        changePassword.addActionListener(e -> changePassword());
        sidebar.add(changePassword);
        sidebar.add(Box.createVerticalStrut(2));

        NavButton signOut = new NavButton("Sign out", Icons.logout(17, Theme.SIDEBAR_TEXT));
        signOut.addActionListener(e -> signOut());
        sidebar.add(signOut);

        sidebar.add(Box.createVerticalStrut(12));
        JLabel version = UiUtils.label("Scholar Manager  •  v1.0", Font.PLAIN, 10,
                new Color(0x64748B));
        version.setAlignmentX(LEFT_ALIGNMENT);
        version.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        sidebar.add(version);

        return sidebar;
    }

    private JPanel buildBrand() {
        JPanel brand = new JPanel(new BorderLayout(10, 0));
        brand.setOpaque(false);
        brand.setBorder(BorderFactory.createEmptyBorder(0, 4, 24, 4));
        brand.setAlignmentX(LEFT_ALIGNMENT);
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));

        brand.add(new JLabel(Icons.logo(38)), BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel name = UiUtils.label("Scholar Manager", Font.BOLD, 15, Color.WHITE);
        name.setAlignmentX(LEFT_ALIGNMENT);
        JLabel role = UiUtils.label("Scholarship Office", Font.PLAIN, 10, Theme.SIDEBAR_MUTED);
        role.setAlignmentX(LEFT_ALIGNMENT);
        text.add(name);
        text.add(Box.createVerticalStrut(1));
        text.add(role);
        brand.add(text, BorderLayout.CENTER);

        return brand;
    }

    private JPanel buildAccountCard() {
        RoundedPanel card = new RoundedPanel()
                .fill(Theme.SIDEBAR_HOVER)
                .borderColor(null)
                .shadow(false)
                .arc(12);
        card.setLayout(new BorderLayout(10, 0));
        card.setBorder(BorderFactory.createEmptyBorder(9, 10, 9, 10));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        RoundedPanel avatar = new RoundedPanel()
                .fill(Theme.PRIMARY)
                .borderColor(null)
                .shadow(false)
                .arc(20);
        avatar.setLayout(new GridBagLayout());
        avatar.setPreferredSize(new Dimension(36, 36));
        avatar.add(UiUtils.label(currentUser.initials(), Font.BOLD, 12, Color.WHITE));
        card.add(avatar, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel name = UiUtils.label(currentUser.displayName(), Font.BOLD, 12, Color.WHITE);
        name.setAlignmentX(LEFT_ALIGNMENT);
        JLabel role = UiUtils.label("Administrator", Font.PLAIN, 10, Theme.SIDEBAR_MUTED);
        role.setAlignmentX(LEFT_ALIGNMENT);
        text.add(name);
        text.add(Box.createVerticalStrut(1));
        text.add(role);
        card.add(text, BorderLayout.CENTER);

        return card;
    }

    private static JLabel sectionLabel(String text) {
        JLabel label = UiUtils.label(text, Font.BOLD, 10, new Color(0x64748B));
        label.setBorder(BorderFactory.createEmptyBorder(6, 10, 8, 0));
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    private void showPage(String key) {
        NavItem item = navItems.stream()
                .filter(nav -> nav.key().equals(key))
                .findFirst()
                .orElse(navItems.get(0));
        currentKey = item.key();
        cardLayout.show(pagesPanel, item.key());
        titleLabel.setText(item.title());
        subtitleLabel.setText(item.subtitle());
        navButtons.forEach((navKey, button) -> button.setActive(navKey.equals(item.key())));

        JComponent page = pages.get(item.key());
        if (page instanceof Refreshable refreshable) {
            refreshable.refresh();
        }
    }

    private void changePassword() {
        AdminUser updated = ChangePasswordDialog.showDialog(this, authService, currentUser);
        if (updated != null) {
            currentUser = updated;
        }
    }

    private void signOut() {
        boolean confirmed = UiUtils.confirm(this, "Sign out",
                "Sign out of Scholar Manager?\nUnsaved form changes will be lost.");
        if (!confirmed) {
            return;
        }
        dispose();
        new LoginFrame(authService, store).setVisible(true);
    }

    private void installShortcuts() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("F5"), "refreshPage");
        getRootPane().getActionMap().put("refreshPage", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComponent page = pages.get(currentKey);
                if (page instanceof Refreshable refreshable) {
                    refreshable.refresh();
                }
            }
        });
    }

    /**
     * Sidebar navigation button with hover and selected states.
     */
    private static final class NavButton extends JButton {

        private boolean active;
        private boolean hover;

        NavButton(String text, ImageIcon icon) {
            super(text, icon);
            setFont(Theme.font(Font.BOLD, 13));
            setForeground(Theme.SIDEBAR_TEXT);
            setHorizontalAlignment(LEFT);
            setIconTextGap(12);
            setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
            setContentAreaFilled(false);
            setOpaque(false);
            setFocusPainted(false);
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            setAlignmentX(LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            setPreferredSize(new Dimension(200, 42));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        void setActive(boolean active) {
            this.active = active;
            setForeground(active ? Color.WHITE : Theme.SIDEBAR_TEXT);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            if (active) {
                g2.setColor(Theme.SIDEBAR_ACTIVE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(0x818CF8));
                g2.fillRoundRect(0, 10, 3, getHeight() - 20, 3, 3);
            } else if (hover) {
                g2.setColor(Theme.SIDEBAR_HOVER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
