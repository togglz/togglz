package org.togglz.slack;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.UserProvider;
import org.togglz.slack.config.NotificationConfiguration;
import org.togglz.slack.notification.Notification;
import org.togglz.slack.notification.NotificationComposer;
import org.togglz.slack.sender.AsyncNotifier;
import org.togglz.slack.sender.NotificationSender;
import org.togglz.slack.sender.Notifier;

/**
 * @author Tomasz Skowroński
 * @since 2.4.0
 */
class SlackNotifications {

    private static final Logger log = LoggerFactory.getLogger(SlackNotifications.class);

    private final NotificationComposer composer;

    private final NotificationSender notificationSender;

    private final ChannelsProvider channelsProvider;

    SlackNotifications(NotificationConfiguration configuration, UserProvider userProvider) {
        this(new NotificationComposer(configuration, userProvider),
                createNotificationSender(configuration),
                new ChannelsProvider(configuration.getChannels()));
    }

    private static NotificationSender createNotificationSender(NotificationConfiguration config) {
        String url = config.getSlackHookUrl();
        return config.isAsyncSenderDisabled() ? new Notifier(url) : new AsyncNotifier(url);
    }

    SlackNotifications(NotificationComposer composer, NotificationSender notificationSender, ChannelsProvider channelsProvider) {
        this.composer = composer;
        this.notificationSender = notificationSender;
        this.channelsProvider = channelsProvider;
    }

    void notify(FeatureState state) {
        List<Notification> notifications = composeNotifications(state);
        log.debug("send " + notifications);
        for (Notification notification : notifications) {
            notificationSender.send(notification);
        }
    }

    private List<Notification> composeNotifications(FeatureState state) {
        return composer.compose(state, channelsProvider.getRecipients());
    }
}
