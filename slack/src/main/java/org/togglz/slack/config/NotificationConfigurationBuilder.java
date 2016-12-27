package org.togglz.slack.config;

import java.util.List;

public final class NotificationConfigurationBuilder {

    private String slackHookUrl;

    private List<String> channels;

    private String togglzAdminConsoleUrl;

    private String appName;

    private String messageFormat;

    private NotificationConfigurationBuilder() {
    }

    public static NotificationConfigurationBuilder create() {
        return new NotificationConfigurationBuilder();
    }

    public NotificationConfigurationBuilder withSlackHookUrl(String slackHookUrl) {
        this.slackHookUrl = slackHookUrl;
        return this;
    }

    public NotificationConfigurationBuilder withChannels(List<String> channels) {
        this.channels = channels;
        return this;
    }

    public NotificationConfigurationBuilder withTogglzAdminConsoleUrl(String togglzAdminConsoleUrl) {
        this.togglzAdminConsoleUrl = togglzAdminConsoleUrl;
        return this;
    }

    public NotificationConfigurationBuilder withAppName(String appName) {
        this.appName = appName;
        return this;
    }

    public NotificationConfigurationBuilder withMessageFormat(String messageFormat) {
        this.messageFormat = messageFormat;
        return this;
    }

    public NotificationConfiguration build() {
        return new NotificationConfiguration(slackHookUrl, channels, togglzAdminConsoleUrl, appName, messageFormat);
    }
}
