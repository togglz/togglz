package org.togglz.core.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Strings {

    private static final Set<String> falseValues = new HashSet<>(4);
    private static final Set<String> trueValues = new HashSet<>(4);

    static {
        falseValues.add("false");
        falseValues.add("off");
        falseValues.add("no");
        falseValues.add("0");

        trueValues.add("true");
        trueValues.add("on");
        trueValues.add("yes");
        trueValues.add("1");
    }

    public static String join(Iterable<?> col, String separator) {
        StringBuilder result = new StringBuilder();
        Iterator<?> it = col.iterator();
        while (it.hasNext()) {
            result.append(it.next());
            if (it.hasNext()) {
                result.append(separator);
            }
        }
        return result.toString();
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().length() == 0;
    }

    public static boolean isNotBlank(String s) {
        return s != null && s.trim().length() > 0;
    }

    /**
     * <p>
     * Returns whether the specified string is empty while remaining null-safe.
     * </p>
     *
     * @param s
     *     the string to be checked (may be {@literal null})
     * @return {@literal true} if {@code s} is either {@literal null} or contains no characters.
     */
    public static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    /**
     * <p>
     * Returns whether the specified string is <b>not</b> empty while remaining null-safe.
     * </p>
     *
     * @param s
     *     the string to be checked (may be {@literal null})
     * @return {@literal true} if {@code s} is not {@literal null} and contains at least one character.
     */
    public static boolean isNotEmpty(String s) {
        return s != null && !s.isEmpty();
    }

    public static List<String> splitAndTrim(String value, String regex) {
        List<String> result = new ArrayList<>();
        if (isNotBlank(value)) {
            String[] segements = value.split(regex);
            for (String segment : segements) {
                if (isNotBlank(segment)) {
                    result.add(segment.trim());
                }
            }
        }
        return result;
    }

    /**
     * <p>
     * Trims the specified string while remaining null-safe.
     * </p>
     *
     * @param s
     *     the string to be trimmed (may be {@literal null})
     * @return The trimmed {@code s} or {@literal null} if {@code s} is {@literal null}.
     */
    public static String trim(String s) {
        return s != null ? s.trim() : null;
    }

    /**
     * <p>
     * Trims the specified string and, if the resulting string is empty, will return {@literal null} instead.
     * </p>
     *
     * @param s
     *     the string to be trimmed (may be {@literal null})
     * @return The trimmed {@code s} or {@literal null} if {@code s} is {@literal null} before or after being trimmed.
     */
    public static String trimToNull(String s) {
        s = trim(s);
        return isNotEmpty(s) ? s : null;
    }

    public static String joinClassNames(Iterable<?> iterable) {
        StringBuilder result = new StringBuilder();
        Iterator<?> it = iterable.iterator();
        while (it.hasNext()) {
            result.append(it.next().getClass().getName());
            if (it.hasNext()) {
                result.append(", ");
            }
        }
        return result.toString();
    }

    /**
     * <p>
     * Attempts to return the best boolean representation for the specified string while remaining null-safe and
     * ignoring any leading/trailing whitespace as well as case.
     * </p>
     * <p>
     * This method simply uses a set of predefined values that it compares {@code s} to in order to determine the best
     * {@code Boolean} representation. If {@code s} does not fall into either category, it is considered invalid and
     * will result in an {@code IllegalArgumentException} being thrown.
     * </p>
     *
     * @param s
     *     the string to be converted into a {@code Boolean}
     * @return The {@code Boolean} representation of {@code s} or {@literal null} if {@code s} is {@literal null}.
     * @throws IllegalArgumentException
     *     If {@code s} is non-{@literal null} <b>and</b> does not match any of the predefined values.
     */
    public static Boolean toBoolean(String s) {
        String value = trimToNull(s);
        if (value == null) {
            return null;
        }

        value = value.toLowerCase();

        if (trueValues.contains(value)) {
            return Boolean.TRUE;
        }
        if (falseValues.contains(value)) {
            return Boolean.FALSE;
        }

        throw new IllegalArgumentException("Invalid boolean value '" + s + "'");
    }
}
