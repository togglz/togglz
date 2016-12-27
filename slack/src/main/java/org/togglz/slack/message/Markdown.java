package org.togglz.slack.message;

import static com.google.common.base.Strings.isNullOrEmpty;

enum Markdown {

    BOLD("*"),
    CODE("`"),
    ITALIC("_"),
    STRIKE("~"),
    PRE("```");

    private final String tag;

    Markdown(String tag) {
        this.tag = tag;
    }

    /**
     * https://api.slack.com/docs/message-formatting#message_formatting
     */
    String format(String text) {
        return isNullOrEmpty(text) ? "" : String.join("", tag, text, tag);
    }

    /**
     * https://api.slack.com/docs/message-formatting#linking_to_urls
     */
    static String link(String url, String name) {
        String urlText = !isNullOrEmpty(url) ? url.trim() : "";
        if (!urlText.isEmpty()) {
            String nameText = !isNullOrEmpty(name) ? name : urlText;
            return String.join("", "<", urlText, "|", nameText, ">");
        } else {
            return name;
        }
    }

    /**
     * https://api.slack.com/docs/message-formatting#linking_to_channels_and_users
     *
     * @param name of user or channel
     */
    static String linkName(String name) {
        return ("<@" + name + ">");
    }
}
