package org.togglz.slack.message;

import java.util.List;

import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.UserProvider;
import org.togglz.slack.config.NotificationConfiguration;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;

public class MessageComposer {

    private static final String SENDER_SUFFIX = "feature toggles";

    private final NotificationConfiguration configuration;

    private final UserProvider userProvider;

    public MessageComposer(NotificationConfiguration configuration, UserProvider userProvider) {
        Preconditions.checkArgument(configuration != null, "configuration is null");
        Preconditions.checkArgument(userProvider != null, "userProvider is null");
        this.configuration = configuration;
        this.userProvider = userProvider;
    }

    public List<Message> compose(FeatureState featureState, List<String> channels) {
        final String text = getText(featureState);
        final String sender = getSender();
        final String icon = EmojiIcon.valueOf(featureState.isEnabled()).format();
        return FluentIterable.from(channels)
            .filter(Functions.IS_NOT_NULL_OR_EMPTY)
            .transform(new Function<String, Message>() {
                @Override
                public Message apply(String channel) {
                    Message message = new Message();
                    message.setChannel(channel);
                    message.setUsername(sender);
                    message.setText(text);
                    message.setIcon(icon);
                    message.setMarkdown(true);
                    return message;
                }
            }).toList();
    }

    private String getText(FeatureState featureState) {
        ImmutableMap<String, String> values = ImmutableMap.<String, String>builder()
            .put("feature", featureState.getFeature().name())
            .put("changed", featureState.isEnabled() ? "enabled" : "disabled")
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
