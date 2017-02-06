package org.togglz.slack.config;

import java.util.Arrays;
import java.util.List;

public final class NotificationConfigurationBuilder {

    private String slackHookUrl;

    private List<String> channels;

    private String togglzAdminConsoleUrl;

    private String appName;

    private String messageFormat;

    private String appIcon;

    private List<String> statesIcons;

    private List<String> changeVerbs;

    private boolean asyncSenderDisabled;

    private boolean labelingEnabled;

    public NotificationConfigurationBuilder withSlackHookUrl(String slackHookUrl) {
        this.slackHookUrl = slackHookUrl;
        return this;
    }

    public NotificationConfigurationBuilder withChannels(String... channels) {
        this.channels = Arrays.asList(channels);
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

    /**
     * @param messageFormat custom replacement for default org.togglz.slack.notification.NotificationComposer.DEFAULT_MESSAGE_FORMAT
     */
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
        this.statesIcons = Arrays.asList(enabled, disabled);
        return this;
    }

    /**
     * @param enabled  verb eg. activated
     * @param disabled verb eg. deactivated
     */
    public NotificationConfigurationBuilder withChangeVerbs(String enabled, String disabled) {
        this.changeVerbs = Arrays.asList(enabled, disabled);
        return this;
    }

    public NotificationConfigurationBuilder disableAsyncSender() {
        this.asyncSenderDisabled = true;
        return this;
    }

    public NotificationConfigurationBuilder enableLabeling() {
        this.labelingEnabled = true;
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
                changeVerbs,
                asyncSenderDisabled,
                labelingEnabled
        );
    }
}
