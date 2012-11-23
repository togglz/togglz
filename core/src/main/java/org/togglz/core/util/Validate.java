package org.togglz.core.util;

import java.util.Collection;

/**
 * Common methods for checking method contracts.
 * 
 * @author Christian Kaltepoth
 */
public class Validate {

    public static void notBlank(String s, String msg) {
        if (s == null || s.trim().length() == 0) {
            throw new IllegalArgumentException(msg);
        }
    }

    public static void notNull(Object o, String msg) {
        if (o == null) {
            throw new IllegalArgumentException(msg);
        }
    }

    public static void notEmpty(Collection<?> c, String msg) {
        if (c == null || c.isEmpty()) {
            throw new IllegalArgumentException(msg);
        }
    }

    public static void isTrue(boolean b, String msg) {
        if (!b) {
            throw new IllegalArgumentException(msg);
        }
    }

}
