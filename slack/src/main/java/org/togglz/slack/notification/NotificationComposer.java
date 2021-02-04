package org.togglz.slack.notification;

import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.UserProvider;
import org.togglz.core.util.FeatureAnnotations;
import org.togglz.core.util.Strings;
import org.togglz.slack.config.NotificationConfiguration;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.togglz.core.util.Preconditions.checkArgument;

public class NotificationComposer {

    private static final String SENDER_SUFFIX = "feature toggles";

    private final NotificationConfiguration configuration;

    private final UserProvider userProvider;

    public NotificationComposer(NotificationConfiguration configuration, UserProvider userProvider) {
        checkArgument(configuration != null, "configuration is null");
        checkArgument(userProvider != null, "userProvider is null");
        this.configuration = configuration;
        this.userProvider = userProvider;
    }

    public List<Notification> compose(FeatureState state, List<String> channels) {
        String message = getMessage(state);
        String appIcon = EmojiIcon.format(configuration.getAppIcon());
        String sender = getSender();
        List<Notification> notifications = new LinkedList<>();
        for (String channel : channels) {
            if (Strings.isNotBlank(channel)) {
                Notification notification = createNotification(message, appIcon, sender, channel);
                notifications.add(notification);
            }
        }
        return notifications;
    }

    private Notification createNotification(String message, String appIcon, String sender, String channel) {
        Notification notification = new Notification();
        notification.setChannel(channel);
        notification.setUsername(sender);
        notification.setText(message);
        notification.setIcon(appIcon);
        notification.setMarkdown(true);
        return notification;
    }

    private String getMessage(final FeatureState state) {
        Map<String, String> values = new HashMap<String, String>() {{
            put("stateIcon", EmojiIcon.format(configuration.getStateIcon(state)));
            put("feature", state.getFeature().name());
            put("changed", configuration.getChangeVerb(state));
            put("user", getUsername());
            put("link", getLink());
        }};
        String format = configuration.getMessageFormat();
        if (configuration.isLabelingEnabled()) {
            format = appendLine(format, formatLabel(state));
        }
        Replacement replacement = new Replacement(values, "$");
        return replacement.replace(format);
    }

    private String getUsername() {
        String name = userProvider.getCurrentUser().getName();
        // Unfortunately Slack replace unknown linked names (ADMIN, UNDEFINED) with ellipsis
        return isFullName(name) ? Markdown.linkName(name) : String.valueOf(name);
    }

    private boolean isFullName(String name) {
        return name != null && name.contains(".");
    }

    private String getSender() {
        return getAppName() + " " + SENDER_SUFFIX;
    }

    private String getAppName() {
        return configuration.getAppName();
    }

    private String getLink() {
        String adminConsoleUrl = configuration.getTogglzAdminConsoleUrl();
        if (Strings.isNotBlank(adminConsoleUrl)) {
            return Markdown.link(adminConsoleUrl, adminConsoleUrl);
        } else {
            return "";
        }
    }

    private String formatLabel(FeatureState state) {
        String label = FeatureAnnotations.getLabel(state.getFeature());
        return Markdown.PRE.format(label);
    }

    private String appendLine(String text, String line) {
        return text + "\n" + line;
    }
}
