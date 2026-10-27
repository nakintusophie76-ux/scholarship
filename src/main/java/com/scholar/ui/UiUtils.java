package com.scholar.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.Locale;

/**
 * Shared UI factory methods. Keeping styling here guarantees that every screen
 * uses the same spacing, colours and table behaviour.
 */
public final class UiUtils {

    private UiUtils() {
    }

    // ------------------------------------------------------------------
    // Labels
    // ------------------------------------------------------------------

    public static JLabel label(String text, int style, int size, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.font(style, size));
        label.setForeground(color);
        return label;
    }

    public static JLabel sectionTitle(String text) {
        return label(text, Font.BOLD, 15, Theme.TEXT);
    }

    public static JLabel muted(String text) {
        return label(text, Font.PLAIN, 12, Theme.MUTED);
    }

    // ------------------------------------------------------------------
    // Buttons
    // ------------------------------------------------------------------

    public static JButton primaryButton(String text) {
        JButton button = baseButton(text);
        button.setBackground(Theme.PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFont(Theme.font(Font.BOLD, 13));
        return button;
    }

    public static JButton secondaryButton(String text) {
        return baseButton(text);
    }

    public static JButton dangerButton(String text) {
        JButton button = baseButton(text);
        button.setBackground(Theme.DANGER_BG);
        button.setForeground(Theme.DANGER);
        button.setFont(Theme.font(Font.BOLD, 13));
        return button;
    }

    public static JButton ghostButton(String text) {
        JButton button = baseButton(text);
        button.putClientProperty("JButton.buttonType", "borderless");
        button.setForeground(Theme.MUTED);
        return button;
    }

    private static JButton baseButton(String text) {
        JButton button = new JButton(text);
        button.setFont(Theme.font(Font.PLAIN, 13));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setMargin(new Insets(8, 16, 8, 16));
        return button;
    }

    // ------------------------------------------------------------------
    // Inputs
    // ------------------------------------------------------------------

    public static JTextField textField(String placeholder) {
        JTextField field = new JTextField();
        styleField(field, placeholder);
        return field;
    }

    public static JPasswordField passwordField(String placeholder) {
        JPasswordField field = new JPasswordField();
        styleField(field, placeholder);
        return field;
    }

    public static JTextField searchField(String placeholder) {
        JTextField field = textField(placeholder);
        field.putClientProperty("JTextField.leadingIcon", Icons.search(16, Theme.MUTED));
        field.putClientProperty("JTextField.showClearButton", true);
        field.setPreferredSize(new Dimension(270, 38));
        return field;
    }

    private static void styleField(JTextField field, String placeholder) {
        field.setFont(Theme.font(Font.PLAIN, 14));
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.setPreferredSize(new Dimension(220, 38));
        field.setMinimumSize(new Dimension(80, 38));
    }

    public static <T> JComboBox<T> comboBox(T[] values) {
        JComboBox<T> combo = new JComboBox<>(values);
        combo.setFont(Theme.font(Font.PLAIN, 14));
        combo.setPreferredSize(new Dimension(200, 38));
        combo.setMinimumSize(new Dimension(100, 38));
        return combo;
    }

    /** Label above a field, as used by all dialogs. */
    public static JPanel labeled(String text, JComponent field) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel label = label(text, Font.BOLD, 12, Theme.MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        Dimension preferred = field.getPreferredSize();
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferred.height));

        panel.add(label);
        panel.add(Box.createVerticalStrut(6));
        panel.add(field);
        return panel;
    }

    public static JScrollPane tableScroll(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(Color.WHITE);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        return scroll;
    }

    // ------------------------------------------------------------------
    // Tables
    // ------------------------------------------------------------------

    public static void styleTable(JTable table) {
        table.setRowHeight(40);
        table.setFont(Theme.font(Font.PLAIN, 13));
        table.setForeground(Theme.TEXT);
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(Theme.PRIMARY_SOFT);
        table.setSelectionForeground(Theme.INK);
        table.setShowGrid(false);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        table.setDefaultRenderer(Object.class, new ZebraRenderer());

        JTableHeader header = table.getTableHeader();
        header.setFont(Theme.font(Font.BOLD, 12));
        header.setForeground(Theme.MUTED);
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));
    }

    public static TableCellRenderer badgeRenderer() {
        return new BadgeRenderer();
    }

    /** Right aligned numeric cell that formats values with the supplied function. */
    public static TableCellRenderer numberRenderer(java.util.function.DoubleFunction<String> format) {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Object shown = value instanceof Number number ? format.apply(number.doubleValue()) : value;
                super.getTableCellRendererComponent(table, shown, isSelected, false, row, column);
                setFont(Theme.font(Font.PLAIN, 13));
                setHorizontalAlignment(SwingConstants.RIGHT);
                setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
                if (isSelected) {
                    setBackground(table.getSelectionBackground());
                    setForeground(table.getSelectionForeground());
                } else {
                    setBackground(row % 2 == 0 ? Color.WHITE : Theme.ROW_ALT);
                    setForeground(Theme.TEXT);
                }
                return this;
            }
        };
    }

    public static TableCellRenderer gpaRenderer() {
        return numberRenderer(com.scholar.util.Money::gpa);
    }

    public static TableCellRenderer moneyRenderer() {
        return numberRenderer(com.scholar.util.Money::format);
    }

    public static Color badgeForeground(Object value) {
        return badgeColors(value)[0];
    }

    private static Color[] badgeColors(Object value) {
        String key = value instanceof Enum<?> e
                ? e.name()
                : value == null ? "" : value.toString().toUpperCase(Locale.ROOT);
        return switch (key) {
            case "APPROVED", "OPEN", "ACTIVE", "ELIGIBLE" ->
                    new Color[]{Theme.SUCCESS, Theme.SUCCESS_BG};
            case "PENDING" -> new Color[]{Theme.WARNING, Theme.WARNING_BG};
            case "WAITLISTED", "APPLIED" -> new Color[]{Theme.INFO, Theme.INFO_BG};
            case "REJECTED", "CLOSED", "INACTIVE", "NOT ELIGIBLE" ->
                    new Color[]{Theme.DANGER, Theme.DANGER_BG};
            case "GRADUATED" -> new Color[]{Theme.NEUTRAL, Theme.NEUTRAL_BG};
            default -> new Color[]{Theme.NEUTRAL, Theme.NEUTRAL_BG};
        };
    }

    private static final class ZebraRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            setFont(Theme.font(Font.PLAIN, 13));
            setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(row % 2 == 0 ? Color.WHITE : Theme.ROW_ALT);
                setForeground(Theme.TEXT);
            }
            return this;
        }
    }

    private static final class BadgeRenderer extends JComponent implements TableCellRenderer {

        private String text = "";
        private Color foreground = Theme.NEUTRAL;
        private Color background = Theme.NEUTRAL_BG;
        private boolean selected;
        private int row;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            this.text = value == null ? "" : value.toString();
            Color[] colors = badgeColors(value);
            this.foreground = colors[0];
            this.background = colors[1];
            this.selected = isSelected;
            this.row = row;
            setFont(Theme.font(Font.BOLD, 12));
            return this;
        }

        @Override
        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(selected ? Theme.PRIMARY_SOFT
                    : (row % 2 == 0 ? Color.WHITE : Theme.ROW_ALT));
            g2.fillRect(0, 0, getWidth(), getHeight());

            FontMetrics metrics = g2.getFontMetrics(getFont());
            int badgeWidth = metrics.stringWidth(text) + 20;
            int badgeHeight = 22;
            int x = 10;
            int y = (getHeight() - badgeHeight) / 2;

            g2.setColor(background);
            g2.fillRoundRect(x, y, badgeWidth, badgeHeight, badgeHeight, badgeHeight);

            g2.setColor(foreground);
            g2.setFont(getFont());
            int textY = y + (badgeHeight - metrics.getHeight()) / 2 + metrics.getAscent();
            g2.drawString(text, x + 10, textY);
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------
    // Dialogs
    // ------------------------------------------------------------------

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Unable to continue",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Scholar Manager",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String title, String message) {
        int answer = JOptionPane.showConfirmDialog(parent, message, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        return answer == JOptionPane.OK_OPTION;
    }

    public static JPanel toolbar() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        return panel;
    }

    public static JLabel rightAligned(String text) {
        JLabel label = muted(text);
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        return label;
    }

    /** Parses a decimal, tolerating thousands separators and a dollar sign. */
    public static double parseDecimal(String text, String fieldName) {
        String cleaned = text == null ? "" : text.trim().replace("$", "").replace(",", "");
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " must be a number, for example 3.75.");
        }
    }

    public static int parseInteger(String text, String fieldName) {
        String cleaned = text == null ? "" : text.trim().replace(",", "");
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        try {
            return Integer.parseInt(cleaned);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " must be a whole number, for example 5.");
        }
    }

    public static String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
