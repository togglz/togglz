package org.togglz.slack.message;

class EmojiIcon {

    static final String ICON_FORMAT = ":%s:";

    static final String TRUE = "large_blue_circle";

    static final String FALSE = "white_circle";

    static final String FLAG_PREFIX = "flag-";

    private final String name;

    public EmojiIcon(String name) {
        this.name = name;
    }

    static EmojiIcon valueOf(String name) {
        return new EmojiIcon(name);
    }

    static EmojiIcon valueOf(boolean enabled) {
        return valueOf(enabled ? TRUE : FALSE);
    }

    static EmojiIcon flagOf(String countryCode) {
        return valueOf(FLAG_PREFIX + countryCode.toLowerCase());
    }

    String format() {
        return String.format(ICON_FORMAT, name);
    }

    @Override
    public String toString() {
        return format();
    }
}
