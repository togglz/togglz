package org.togglz.slack.notification;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.UserProvider;
import org.togglz.slack.config.NotificationConfiguration;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public class NotificationComposer {

    public static final String DEFAULT_MESSAGE_FORMAT = "$stateIcon *$feature* was $changed by $user $link";

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
        final String message = getMessage(state);
        final String appIcon = EmojiIcon.format(configuration.getAppIcon());
        final String sender = getSender();
        return FluentIterable.from(channels)
                .filter(Predicates.notNull())
                .transform(new Function<String, Notification>() {
                    @Override
                    public Notification apply(String channel) {
                        Notification notification = new Notification();
                        notification.setChannel(channel);
                        notification.setUsername(sender);
                        notification.setText(message);
                        notification.setIcon(appIcon);
                        notification.setMarkdown(true);
                        return notification;
                    }
                }).toList();
    }

    private String getMessage(FeatureState state) {
        ImmutableMap<String, String> values = ImmutableMap.<String, String>builder()
                .put("stateIcon", EmojiIcon.format(configuration.getStateIcon(state)))
                .put("feature", state.getFeature().name())
                .put("changed", configuration.getChangeVerb(state))
                .put("user", getUsername())
                .put("link", getLink())
                .build();
        Replacement replacement = new Replacement(values, "$");
        return replacement.replace(configuration.getMessageFormat());
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
        return Joiner.on(" ").join(getAppName(), SENDER_SUFFIX);
    }

    private String getAppName() {
        return configuration.getAppName();
    }

    private String getLink() {
        String adminConsoleUrl = configuration.getTogglzAdminConsoleUrl();
        if (!Strings.isNullOrEmpty(adminConsoleUrl)) {
            return Markdown.link(adminConsoleUrl, adminConsoleUrl);
        } else {
            return "";
        }
    }
}
