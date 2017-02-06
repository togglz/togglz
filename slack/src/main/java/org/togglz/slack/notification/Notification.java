package org.togglz.slack.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.togglz.core.util.MoreObjects;

public class Notification {

    @JsonProperty("channel")
    private String channel;

    @JsonProperty("username")
    private String username;

    @JsonProperty("text")
    private String text;

    @JsonProperty("icon_emoji")
    private String icon;

    @JsonProperty("mrkdwn")
    private boolean markdown;

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isMarkdown() {
        return markdown;
    }

    public void setMarkdown(boolean markdown) {
        this.markdown = markdown;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("channel", channel)
                .add("username", username)
                .add("text", text)
                .add("icon", icon)
                .add("markdown", markdown)
                .toString();
    }
}
