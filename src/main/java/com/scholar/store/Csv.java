package com.scholar.store;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal, dependency free CSV reader/writer.
 *
 * <p>Fields containing commas or quotes are wrapped in double quotes and
 * embedded quotes are doubled. Line breaks inside values are flattened to a
 * space so every record always occupies exactly one line.</p>
 */
public final class Csv {

    private Csv() {
    }

    public static String line(String... values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escape(values[i]));
        }
        return sb.toString();
    }

    public static String escape(String value) {
        String text = value == null ? "" : value
                .replace("\r\n", " ")
                .replace('\r', ' ')
                .replace('\n', ' ');
        if (text.indexOf(',') >= 0 || text.indexOf('"') >= 0
                || text.startsWith(" ") || text.endsWith(" ")) {
            return '"' + text.replace("\"", "\"\"") + '"';
        }
        return text;
    }

    public static List<String> parse(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        quoted = false;
                    }
                } else {
                    current.append(c);
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == ',') {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        values.add(current.toString());
        return values;
    }

    public static String text(List<String> row, int index) {
        return index < row.size() ? row.get(index) : "";
    }

    public static double decimal(List<String> row, int index, double fallback) {
        try {
            return Double.parseDouble(text(row, index).trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    public static int integer(List<String> row, int index, int fallback) {
        try {
            return Integer.parseInt(text(row, index).trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    public static boolean bool(List<String> row, int index, boolean fallback) {
        String value = text(row, index).trim();
        if (value.isEmpty()) {
            return fallback;
        }
        return Boolean.parseBoolean(value);
    }

    public static java.time.LocalDate date(List<String> row, int index) {
        String value = text(row, index).trim();
        if (value.isEmpty()) {
            return null;
        }
        try {
            return java.time.LocalDate.parse(value);
        } catch (java.time.format.DateTimeParseException ex) {
            return null;
        }
    }

    public static java.time.LocalDateTime dateTime(List<String> row, int index) {
        String value = text(row, index).trim();
        if (value.isEmpty()) {
            return null;
        }
        try {
            return java.time.LocalDateTime.parse(value);
        } catch (java.time.format.DateTimeParseException ex) {
            return null;
        }
    }
}
