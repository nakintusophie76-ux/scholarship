package com.scholar.util;

import java.util.Collection;
import java.util.Locale;

/**
 * Generates human friendly sequential identifiers such as STU-0007.
 */
public final class Ids {

    private Ids() {
    }

    public static String next(String prefix, Collection<String> existingIds) {
        int max = 0;
        String marker = prefix + "-";
        for (String id : existingIds) {
            if (id == null || !id.startsWith(marker)) {
                continue;
            }
            try {
                max = Math.max(max, Integer.parseInt(id.substring(marker.length())));
            } catch (NumberFormatException ignored) {
                // Ignore malformed identifiers, they are not part of the sequence.
            }
        }
        return String.format(Locale.ROOT, "%s-%04d", prefix, max + 1);
    }
}
