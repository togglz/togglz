package org.togglz.core.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Strings {

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
    
    public static boolean equalsIgnoreCase(String s, String v) {
        return s != null && s.trim().equalsIgnoreCase(v);
    }

    public static List<String> splitAndTrim(String value, String regex) {
        List<String> result = new ArrayList<String>();
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

    public static String trimToNull(String s) {
        if(s != null && s.trim().length() > 0) {
            return s.trim();
        }
        return null;
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

}
