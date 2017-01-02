package org.togglz.slack.config;

import com.google.common.collect.ImmutableList;

import java.util.List;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.togglz.core.util.Strings.isNotBlank;

public class NotificationConfiguration {

    private static final List<String> DEFAULT_CHANNELS = ImmutableList.of("toggles");
    private static final String DEFAULT_MESSAGE_FORMAT = "$feature was $changed by $user $link";

    private final String slackHookUrl;
    private final List<String> channels;
    private final String togglzAdminConsoleUrl;
    private final String appName;
    private final String messageFormat;

    public static NotificationConfigurationBuilder builder() {
        return NotificationConfigurationBuilder.create();
    }

    NotificationConfiguration(String slackHookUrl, List<String> channels, String togglzAdminConsoleUrl, String appName, String messageFormat) {
        checkArgument(isNotBlank(slackHookUrl), "slackHookUrl is required");
        checkArgument(slackHookUrl.startsWith("http"), "slackHookUrl isn't valid url: %s", slackHookUrl);
        this.slackHookUrl = slackHookUrl;
        this.channels = firstNonNull(channels, DEFAULT_CHANNELS);
        this.togglzAdminConsoleUrl = firstNonNull(togglzAdminConsoleUrl, "");
        this.appName = firstNonNull(appName, "");
        this.messageFormat = firstNonNull(messageFormat, DEFAULT_MESSAGE_FORMAT);
    }

    public String getSlackHookUrl() {
        return slackHookUrl;
    }

    public List<String> getChannels() {
        return channels;
    }

    public String getTogglzAdminConsoleUrl() {
        return togglzAdminConsoleUrl;
    }

    public String getAppName() {
        return appName;
    }

    public String getMessageFormat() {
        return messageFormat;
    }
}
