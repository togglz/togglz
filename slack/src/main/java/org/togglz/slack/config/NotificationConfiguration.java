package org.togglz.slack.config;

import org.togglz.core.repository.FeatureState;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.togglz.core.util.MoreObjects.firstNonNull;
import static org.togglz.core.util.Preconditions.checkArgument;
import static org.togglz.core.util.Strings.isNotBlank;

public class NotificationConfiguration {

    private static final String DEFAULT_MESSAGE_FORMAT = "$stateIcon *$feature* was $changed by $user $link";

    private static final List<String> DEFAULT_CHANNELS = Collections.singletonList("toggles");
    private static final List<String> DEFAULT_STATE_ICONS = Arrays.asList("large_blue_circle", "white_circle");
    private static final List<String> DEFAULT_CHANGE_VERBS = Arrays.asList("enabled", "disabled");
    private static final String DEFAULT_APP_ICON = "joystick";

    private final String slackHookUrl;
    private final List<String> channels;
    private final String togglzAdminConsoleUrl;
    private final String appName;
    private final String messageFormat;
    private final String appIcon;
    private final List<String> stateIcons;
    private final List<String> changeVerbs;
    private final boolean asyncSenderDisabled;
    private final boolean labelingEnabled;

    public static NotificationConfigurationBuilder builder() {
        return new NotificationConfigurationBuilder();
    }

    NotificationConfiguration(String slackHookUrl,
                              List<String> channels,
                              String togglzAdminConsoleUrl,
                              String appName,
                              String messageFormat,
                              String appIcon,
                              List<String> stateIcons,
                              List<String> changeVerbs,
                              boolean asyncSenderDisabled,
                              boolean labelingEnabled) {
        checkArgument(isNotBlank(slackHookUrl), "slackHookUrl is required");
        checkArgument(slackHookUrl.startsWith("http"), "slackHookUrl isn't valid url: %s", slackHookUrl);
        this.slackHookUrl = slackHookUrl;
        this.channels = channels != null ? new LinkedList<>(channels) : DEFAULT_CHANNELS;
        this.togglzAdminConsoleUrl = firstNonNull(togglzAdminConsoleUrl, "");
        this.appName = firstNonNull(appName, "");
        this.messageFormat = firstNonNull(messageFormat, DEFAULT_MESSAGE_FORMAT);
        this.appIcon = firstNonNull(appIcon, DEFAULT_APP_ICON);
        this.stateIcons = stateIcons != null ? new LinkedList<>(stateIcons) : DEFAULT_STATE_ICONS;
        this.changeVerbs = changeVerbs != null ? new LinkedList<>(changeVerbs) : DEFAULT_CHANGE_VERBS;
        this.asyncSenderDisabled = asyncSenderDisabled;
        this.labelingEnabled = labelingEnabled;
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

    public String getAppIcon() {
        return appIcon;
    }

    public String getStateIcon(FeatureState state) {
        return stateIcons.get(state.isEnabled() ? 0 : 1);
    }

    public String getChangeVerb(FeatureState state) {
        return changeVerbs.get(state.isEnabled() ? 0 : 1);
    }

    public boolean isAsyncSenderDisabled() {
        return asyncSenderDisabled;
    }

    public boolean isLabelingEnabled() {
        return labelingEnabled;
    }
}
