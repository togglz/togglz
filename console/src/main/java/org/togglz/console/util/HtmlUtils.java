package org.togglz.console.util;

public class HtmlUtils {

    public static String escape(String string) {
        if (string == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            switch (c) {
                case '<':
                    result.append("&lt;");
                    break;
                case '>':
                    result.append("&gt;");
                    break;
                case '&':
                    result.append("&amp;");
                    break;
                case '"':
                    result.append("&quot;");
                    break;
                case '\'':
                    result.append("&#x27;");
                    break;
                default:
                    result.append(c);
            }
        }
        return result.toString();
    }

}
