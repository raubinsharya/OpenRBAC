package com.open.rbac.openrbac.utils;

import java.util.regex.Pattern;

public class ParsingUtils {

    private static final Pattern NUMERIC_PATTERN = Pattern.compile("-?\\d+");

    /**
     * Safely attempts to parse a String to a Long.
     * Returns null if the string cannot be parsed.
     *
     * @param value The string to parse.
     * @return The Long value, or null if parsing fails.
     */
    public static Long safeParseLong(String value) {
        if (value == null || !NUMERIC_PATTERN.matcher(value).matches()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
