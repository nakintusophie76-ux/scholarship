package com.scholar.util;

import java.util.Locale;

/**
 * Money and GPA formatting. Kept in one place so every screen shows the
 * same representation of a value.
 */
public final class Money {

    private Money() {
    }

    public static String format(double amount) {
        String text = String.format(Locale.US, "%,.2f", amount);
        if (text.endsWith(".00")) {
            text = text.substring(0, text.length() - 3);
        }
        return "$" + text;
    }

    /** Raw value for CSV export, always with two decimals and no grouping. */
    public static String plain(double amount) {
        return String.format(Locale.ROOT, "%.2f", amount);
    }

    public static String gpa(double gpa) {
        return String.format(Locale.ROOT, "%.2f", gpa);
    }
}
