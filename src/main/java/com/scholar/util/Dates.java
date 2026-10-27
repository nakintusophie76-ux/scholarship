package com.scholar.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * Date formatting helpers shared by the whole application.
 */
public final class Dates {

    public static final DateTimeFormatter DISPLAY_DATE =
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    public static final DateTimeFormatter DISPLAY_DATE_TIME =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH);

    private Dates() {
    }

    public static String format(LocalDate date) {
        return date == null ? "—" : DISPLAY_DATE.format(date);
    }

    public static String format(LocalDateTime dateTime) {
        return dateTime == null ? "—" : DISPLAY_DATE_TIME.format(dateTime);
    }

    /** Short badge used next to a deadline, for example "12 days left". */
    public static String deadlineNote(LocalDate deadline) {
        if (deadline == null) {
            return "No deadline";
        }
        long days = ChronoUnit.DAYS.between(LocalDate.now(), deadline);
        if (days < -1) {
            return Math.abs(days) + " days ago";
        }
        if (days == -1) {
            return "Closed yesterday";
        }
        if (days == 0) {
            return "Closes today";
        }
        if (days == 1) {
            return "1 day left";
        }
        return days + " days left";
    }
}
