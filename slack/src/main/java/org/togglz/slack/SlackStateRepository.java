package org.togglz.slack;

import org.togglz.core.Feature;
import org.togglz.core.logging.Log;
import org.togglz.core.logging.LogFactory;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.user.UserProvider;
import org.togglz.slack.config.NotificationConfiguration;
import org.togglz.slack.notification.Notification;
import org.togglz.slack.notification.NotificationComposer;
import org.togglz.slack.sender.AsyncNotifier;
import org.togglz.slack.sender.NotificationSender;
import org.togglz.slack.sender.Notifier;

import java.util.List;

/**
 * @author Tomasz Skowroński
 * @since 2.4.0
 */
public class SlackStateRepository implements StateRepository {

    private static final Log log = LogFactory.getLog(SlackStateRepository.class);

    private final NotificationComposer composer;

    private final NotificationSender notificationSender;

    private final ChannelsProvider channelsProvider;

    public SlackStateRepository(NotificationConfiguration configuration, UserProvider userProvider) {
        this(new NotificationComposer(configuration, userProvider),
                createNotificationSender(configuration),
                new ChannelsProvider(configuration.getChannels()));
    }

    private static NotificationSender createNotificationSender(NotificationConfiguration config) {
        String url = config.getSlackHookUrl();
        return config.isDisabledAsyncSender() ? new Notifier(url) : new AsyncNotifier(url);
    }

    public SlackStateRepository(NotificationComposer composer, NotificationSender notificationSender, ChannelsProvider channelsProvider) {
        this.composer = composer;
        this.notificationSender = notificationSender;
        this.channelsProvider = channelsProvider;
    }

    @Override
    public FeatureState getFeatureState(Feature ignored) {
        return null;
    }

    @Override
    public void setFeatureState(FeatureState state) {
        List<Notification> notifications = composeNotifications(state);
        log.info("send " + notifications);
        for (Notification notification : notifications) {
            notificationSender.send(notification);
        }
    }

    private List<Notification> composeNotifications(FeatureState state) {
        return composer.compose(state, channelsProvider.getRecipients());
    }
}
