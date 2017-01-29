package org.togglz.slack.notification;

import org.togglz.core.util.Strings;

import java.util.Arrays;

public enum Markdown {

    BOLD("*"),
    CODE("`"),
    ITALIC("_"),
    STRIKE("~"),
    PRE("```");

    private final String tag;

    private Markdown(String tag) {
        this.tag = tag;
    }

    /**
     * https://api.slack.com/docs/message-formatting#message_formatting
     */
    public String format(String text) {
        return Strings.isBlank(text) ? "" : concat(tag, text, tag);
    }

    /**
     * https://api.slack.com/docs/message-formatting#linking_to_urls
     */
    public static String link(String url, String name) {
        String urlText = Strings.isNotBlank(url) ? url.trim() : "";
        if (!urlText.isEmpty()) {
            String nameText = Strings.isNotBlank(name) ? name : urlText;
            return concat("<", urlText, "|", nameText, ">");
        } else {
            return name;
        }
    }

    /**
     * https://api.slack.com/docs/message-formatting#linking_to_channels_and_users
     *
     * @param name of user or channel
     */
    public static String linkName(String name) {
        return ("<@" + name + ">");
    }

    private static String concat(String... strings) {
        return Strings.join(Arrays.asList(strings), "");
    }
}
