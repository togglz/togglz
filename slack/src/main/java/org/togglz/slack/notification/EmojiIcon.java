package org.togglz.slack.notification;

public class EmojiIcon {

    private static final String ICON_FORMAT = ":%s:";

    private static final String FLAG_PREFIX = "flag-";

    private final String name;

    private EmojiIcon(String name) {
        this.name = name.replaceAll(":", "");
    }

    public static String format(String name) {
        return new EmojiIcon(name).toString();
    }

    public static String flagOf(String countryCode) {
        return format(FLAG_PREFIX + countryCode.toLowerCase());
    }

    @Override
    public String toString() {
        return String.format(ICON_FORMAT, name);
    }
}
