package de.chkal.togglz.core.util;

import java.util.Collection;
import java.util.Iterator;

public class Strings {

    public static String join(Collection<String> col, String separator) {
        StringBuilder result = new StringBuilder();
        Iterator<String> it = col.iterator();
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

}
