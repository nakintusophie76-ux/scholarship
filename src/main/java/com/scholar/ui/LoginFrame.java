package com.scholar.ui;

import com.scholar.model.AdminUser;
import com.scholar.model.Student;
import com.scholar.model.StudentAccount;
import com.scholar.service.AuthService;
import com.scholar.service.StudentAccountService;
import com.scholar.service.ValidationException;
import com.scholar.store.DataStore;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;

/**
 * Sign in screen. The left panel carries the brand, the right panel holds the
 * credential form, the student / administrator role switch and the demo
 * account hints.
 */
public final class LoginFrame extends JFrame {

    private final AuthService authService;
    private final StudentAccountService accountService;
    private final DataStore store;

    private final JComboBox<String> roleCombo =
            UiUtils.comboBox(new String[]{"Administrator", "Student"});
    private final JTextField usernameField = UiUtils.textField("admin");
    private final JPasswordField passwordField = UiUtils.passwordField("Your password");
    private final JLabel errorLabel = UiUtils.label(" ", Font.BOLD, 12, Theme.DANGER);
    private final JLabel subLabel = UiUtils.label("Sign in to the scholarship office workspace.",
            Font.PLAIN, 13, Theme.MUTED);
    private final JLabel demoTitle = UiUtils.label("DEMO ADMIN ACCOUNT",
            Font.BOLD, 10, Theme.PRIMARY_DARK);
    private final JLabel demoText = UiUtils.label("Username: admin     Password: admin123",
            Font.PLAIN, 12, Theme.TEXT);
    private final JButton signInButton = UiUtils.primaryButton("Sign in");
    private final JButton registerButton =
            UiUtils.ghostButton("New student? Create an account");

    public LoginFrame(AuthService authService, DataStore store) {
        super("Scholar Manager — Sign in");
        this.authService = authService;
        this.accountService = new StudentAccountService(store);
        this.store = store;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setIconImage(Icons.appIcon());
        setSize(1000, 660);
        setMinimumSize(new Dimension(900, 620));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new GridLayout(1, 2));
        root.add(new BrandPanel());
        root.add(buildFormPanel());
        setContentPane(root);

        getRootPane().setDefaultButton(signInButton);
        updateRole();
    }

    private JPanel buildFormPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(Theme.CARD);
        wrapper.setBorder(BorderFactory.createEmptyBorder(24, 44, 24, 44));

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(380, 560));
        form.setMaximumSize(new Dimension(380, Integer.MAX_VALUE));

        JLabel heading = UiUtils.label("Welcome back", Font.BOLD, 28, Theme.INK);
        heading.setAlignmentX(LEFT_ALIGNMENT);
        subLabel.setAlignmentX(LEFT_ALIGNMENT);

        usernameField.setPreferredSize(new Dimension(380, 38));
        passwordField.setPreferredSize(new Dimension(380, 38));
        roleCombo.setPreferredSize(new Dimension(380, 38));
        roleCombo.addActionListener(e -> updateRole());

        JCheckBox showPassword = new JCheckBox("Show password");
        showPassword.setOpaque(false);
        showPassword.setFont(Theme.font(Font.PLAIN, 12));
        showPassword.setForeground(Theme.MUTED);
        showPassword.setFocusPainted(false);
        showPassword.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        showPassword.setAlignmentX(LEFT_ALIGNMENT);
        showPassword.addActionListener(e -> passwordField.setEchoChar(
                showPassword.isSelected() ? (char) 0 : '\u2022'));

        errorLabel.setAlignmentX(LEFT_ALIGNMENT);
        errorLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        signInButton.setAlignmentX(LEFT_ALIGNMENT);
        signInButton.setFont(Theme.font(Font.BOLD, 14));
        signInButton.setPreferredSize(new Dimension(380, 44));
        signInButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        signInButton.addActionListener(e -> signIn());

        registerButton.setAlignmentX(LEFT_ALIGNMENT);
        registerButton.setFont(Theme.font(Font.BOLD, 12));
        registerButton.setPreferredSize(new Dimension(380, 30));
        registerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        registerButton.addActionListener(e -> register());

        RoundedPanel demo = new RoundedPanel()
                .fill(Theme.PRIMARY_SOFT)
                .borderColor(Theme.PRIMARY_SOFT)
                .shadow(false)
                .arc(12);
        demo.setLayout(new BoxLayout(demo, BoxLayout.Y_AXIS));
        demo.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        demo.setAlignmentX(LEFT_ALIGNMENT);
        demo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        demoTitle.setAlignmentX(LEFT_ALIGNMENT);
        demoText.setAlignmentX(LEFT_ALIGNMENT);
        demo.add(demoTitle);
        demo.add(Box.createVerticalStrut(3));
        demo.add(demoText);

        form.add(heading);
        form.add(Box.createVerticalStrut(4));
        form.add(subLabel);
        form.add(Box.createVerticalStrut(18));
        form.add(field("Sign in as", roleCombo));
        form.add(Box.createVerticalStrut(12));
        form.add(field("Username", usernameField));
        form.add(Box.createVerticalStrut(12));
        form.add(field("Password", passwordField));
        form.add(Box.createVerticalStrut(8));
        form.add(showPassword);
        form.add(Box.createVerticalStrut(4));
        form.add(errorLabel);
        form.add(Box.createVerticalStrut(6));
        form.add(signInButton);
        form.add(Box.createVerticalStrut(8));
        form.add(registerButton);
        form.add(Box.createVerticalStrut(12));
        form.add(demo);
        form.add(Box.createVerticalGlue());

        wrapper.add(form);
        return wrapper;
    }

    private static JPanel field(String label, JComponent input) {
        JPanel panel = UiUtils.labeled(label, input);
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        input.setPreferredSize(new Dimension(380, 38));
        return panel;
    }

    private void updateRole() {
        boolean student = roleCombo.getSelectedIndex() == 1;
        errorLabel.setText(" ");
        subLabel.setText(student
                ? "Apply for scholarships and track your decisions."
                : "Sign in to the scholarship office workspace.");
        registerButton.setVisible(student);

        if (student) {
            if (usernameField.getText() == null || usernameField.getText().trim().isEmpty()
                    || "admin".equals(usernameField.getText().trim())) {
                usernameField.setText("");
            }
            usernameField.putClientProperty("JTextField.placeholderText", "Your student username");
            demoTitle.setText("DEMO STUDENT ACCOUNTS");
            demoText.setText("<html>amina / student123 &nbsp;•&nbsp; sofia / student123"
                    + "<br>Or create your own account below.</html>");
        } else {
            if (usernameField.getText() == null || usernameField.getText().trim().isEmpty()) {
                usernameField.setText("admin");
            }
            usernameField.putClientProperty("JTextField.placeholderText", "admin");
            demoTitle.setText("DEMO ADMIN ACCOUNT");
            demoText.setText("Username: admin     Password: admin123");
        }
        usernameField.requestFocusInWindow();
    }

    private void signIn() {
        errorLabel.setText(" ");
        signInButton.setEnabled(false);
        try {
            String password = new String(passwordField.getPassword());
            if (roleCombo.getSelectedIndex() == 1) {
                StudentAccount account = accountService.login(usernameField.getText(), password);
                Student student = store.findStudent(account.studentId());
                if (student == null) {
                    throw new ValidationException(
                            "This portal account is no longer linked to a student record.");
                }
                dispose();
                new StudentMainFrame(store, account).setVisible(true);
            } else {
                AdminUser user = authService.login(usernameField.getText(), password);
                dispose();
                new MainFrame(store, user).setVisible(true);
            }
        } catch (ValidationException ex) {
            errorLabel.setText(ex.getMessage());
            passwordField.requestFocusInWindow();
            passwordField.selectAll();
        } finally {
            signInButton.setEnabled(true);
        }
    }

    private void register() {
        StudentAccount account = StudentRegistrationDialog.showDialog(this, accountService);
        if (account != null) {
            dispose();
            new StudentMainFrame(store, account).setVisible(true);
        }
    }

    /**
     * Brand panel with a gradient background and decorative translucent shapes.
     */
    private static final class BrandPanel extends JPanel {

        BrandPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createEmptyBorder(48, 48, 40, 48));
            setOpaque(false);

            JLabel logo = new JLabel(Icons.logo(60));
            logo.setAlignmentX(LEFT_ALIGNMENT);

            JLabel name = UiUtils.label("Scholar Manager", Font.BOLD, 30, Color.WHITE);
            name.setAlignmentX(LEFT_ALIGNMENT);

            JLabel tagline = UiUtils.label("Scholarship Management System",
                    Font.PLAIN, 14, new Color(0xC7D2FE));
            tagline.setAlignmentX(LEFT_ALIGNMENT);

            add(logo);
            add(Box.createVerticalStrut(22));
            add(name);
            add(Box.createVerticalStrut(6));
            add(tagline);
            add(Box.createVerticalStrut(36));
            add(feature("Keep every student record in one place"));
            add(Box.createVerticalStrut(12));
            add(feature("Apply and track funding in the student portal"));
            add(Box.createVerticalStrut(12));
            add(feature("Export award reports in seconds"));
            add(Box.createVerticalGlue());

            JLabel footer = UiUtils.label("Students and scholarship office  •  Academic year 2026/27",
                    Font.PLAIN, 11, new Color(0x94A3B8));
            footer.setAlignmentX(LEFT_ALIGNMENT);
            add(footer);
        }

        private static JPanel feature(String text) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            row.setOpaque(false);
            row.setAlignmentX(LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
            row.add(new JLabel(Icons.check(16, new Color(0xA7F3D0))));
            row.add(UiUtils.label(text, Font.PLAIN, 13, new Color(0xE0E7FF)));
            return row;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, Theme.PRIMARY_DARK,
                    getWidth(), getHeight(), new Color(0x7C3AED)));
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(new Color(255, 255, 255, 10));
            g2.fillOval(getWidth() - 200, -110, 340, 340);
            g2.fillOval(-120, getHeight() - 190, 300, 300);
            g2.setColor(new Color(255, 255, 255, 8));
            g2.fillOval(getWidth() - 300, getHeight() - 240, 260, 260);
            g2.dispose();

            super.paintComponent(g);
        }
    }
}
