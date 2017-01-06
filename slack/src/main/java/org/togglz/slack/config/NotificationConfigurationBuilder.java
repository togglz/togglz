package org.togglz.slack.config;

import com.google.common.collect.ImmutableList;

import java.util.List;

public final class NotificationConfigurationBuilder {

    private String slackHookUrl;

    private List<String> channels;

    private String togglzAdminConsoleUrl;

    private String appName;

    private String messageFormat;

    private String appIcon;

    private List<String> statesIcons;

    private boolean disabledAsyncSender;

    public NotificationConfigurationBuilder withSlackHookUrl(String slackHookUrl) {
        this.slackHookUrl = slackHookUrl;
        return this;
    }

    public NotificationConfigurationBuilder withChannels(String... channels) {
        this.channels = ImmutableList.copyOf(channels);
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

    /**
     * @param appIcon name, eg. robot_face
     */
    public NotificationConfigurationBuilder withAppIcon(String appIcon) {
        this.appIcon = appIcon;
        return this;
    }

    /**
     * @param enabled  icon name, eg. green_apple
     * @param disabled icon name, eg. apple
     */
    public NotificationConfigurationBuilder withStatesIcons(String enabled, String disabled) {
        this.statesIcons = ImmutableList.of(enabled, disabled);
        return this;
    }

    public NotificationConfigurationBuilder disableAsyncSender() {
        this.disabledAsyncSender = true;
        return this;
    }

    public NotificationConfiguration build() {
        return new NotificationConfiguration(
                slackHookUrl,
                channels,
                togglzAdminConsoleUrl,
                appName,
                messageFormat,
                appIcon,
                statesIcons,
                disabledAsyncSender
        );
    }
}
