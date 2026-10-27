package com.scholar.ui;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;

/**
 * Central colour palette and look and feel setup.
 */
public final class Theme {

    public static final Color PRIMARY = new Color(0x4F46E5);
    public static final Color PRIMARY_DARK = new Color(0x312E81);
    public static final Color PRIMARY_SOFT = new Color(0xEEF2FF);
    public static final Color INK = new Color(0x0F172A);
    public static final Color TEXT = new Color(0x1E293B);
    public static final Color MUTED = new Color(0x64748B);
    public static final Color BG = new Color(0xF1F5F9);
    public static final Color CARD = Color.WHITE;
    public static final Color BORDER = new Color(0xE2E8F0);
    public static final Color ROW_ALT = new Color(0xF8FAFC);

    public static final Color SIDEBAR = new Color(0x0F172A);
    public static final Color SIDEBAR_HOVER = new Color(0x1E293B);
    public static final Color SIDEBAR_ACTIVE = new Color(0x312E81);
    public static final Color SIDEBAR_TEXT = new Color(0xCBD5E1);
    public static final Color SIDEBAR_MUTED = new Color(0x94A3B8);

    public static final Color SUCCESS = new Color(0x047857);
    public static final Color SUCCESS_BG = new Color(0xD1FAE5);
    public static final Color WARNING = new Color(0xB45309);
    public static final Color WARNING_BG = new Color(0xFEF3C7);
    public static final Color DANGER = new Color(0xB91C1C);
    public static final Color DANGER_BG = new Color(0xFEE2E2);
    public static final Color INFO = new Color(0x1D4ED8);
    public static final Color INFO_BG = new Color(0xDBEAFE);
    public static final Color NEUTRAL = new Color(0x475569);
    public static final Color NEUTRAL_BG = new Color(0xF1F5F9);

    public static final Color CHART_1 = new Color(0xF59E0B);
    public static final Color CHART_2 = new Color(0x10B981);
    public static final Color CHART_3 = new Color(0x3B82F6);
    public static final Color CHART_4 = new Color(0xEF4444);

    private Theme() {
    }

    public static Font font(int style, int size) {
        return new Font("Segoe UI", style, size);
    }

    public static void install() {
        FlatLightLaf.setup();

        UIManager.put("defaultFont", font(Font.PLAIN, 14));
        UIManager.put("Component.arc", 10);
        UIManager.put("Button.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.innerFocusWidth", 0);
        UIManager.put("Component.focusColor", PRIMARY);
        UIManager.put("ScrollBar.width", 11);
        UIManager.put("ScrollBar.thumbArc", 11);
        UIManager.put("ScrollBar.trackArc", 11);
        UIManager.put("ScrollBar.showButtons", false);
        UIManager.put("Table.showHorizontalLines", false);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("TableHeader.separatorColor", BORDER);
        UIManager.put("TableHeader.bottomSeparatorColor", BORDER);
        UIManager.put("TableHeader.background", CARD);
        UIManager.put("TableHeader.foreground", MUTED);
        UIManager.put("Table.selectionBackground", PRIMARY_SOFT);
        UIManager.put("Table.selectionForeground", INK);
        UIManager.put("Table.gridColor", BORDER);
        UIManager.put("Panel.background", BG);
        UIManager.put("OptionPane.messageFont", font(Font.PLAIN, 14));
        UIManager.put("OptionPane.buttonFont", font(Font.PLAIN, 13));
        UIManager.put("ToolTip.background", INK);
        UIManager.put("ToolTip.foreground", Color.WHITE);
    }
}
