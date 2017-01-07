package org.togglz.slack.notification;

public class EmojiIcon {

    private static final String ICON_FORMAT = ":%s:";

    private static final String FLAG_PREFIX = "flag-";

    private final String name;

    private EmojiIcon(String name) {
        this.name = name.replaceAll(":", "");
    }

    public static EmojiIcon valueOf(String name) {
        return new EmojiIcon(name);
    }

    public static EmojiIcon flagOf(String countryCode) {
        return valueOf(FLAG_PREFIX + countryCode.toLowerCase());
    }

    public String format() {
        return String.format(ICON_FORMAT, name);
    }

    @Override
    public String toString() {
        return format();
    }
}
