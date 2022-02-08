package org.togglz.slack;

import org.junit.jupiter.api.Test;
import org.togglz.core.user.SingleUserProvider;
import org.togglz.core.user.UserProvider;
import org.togglz.slack.notification.Notification;
import org.togglz.slack.notification.NotificationComposer;
import org.togglz.slack.sender.NotificationSender;

import java.util.LinkedList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.togglz.FeatureFixture.ENABLE_F1;
import static org.togglz.slack.NotificationConfigurationFixture.configureChannels;

class SlackNotificationsTest {

    List<String> developers;
    List<String> developersQaEngineers;

    public SlackNotificationsTest() {
        this.developers = new LinkedList<>();
        this.developers.add("developers");

        this.developersQaEngineers = new LinkedList<>();
        this.developersQaEngineers.add("developers");
        this.developersQaEngineers.add("qa-engineers");
    }

    @Test
    void shouldSendNotificationToChannels() {
        UserProvider userProvider = new SingleUserProvider("someName");
        NotificationComposer composer = new NotificationComposer(configureChannels(new LinkedList<>()), userProvider);
        NotificationSender notificationSenderMock = mock(NotificationSender.class);
        SlackNotifications slackNotifications = new SlackNotifications(composer, notificationSenderMock, new ChannelsProvider(new LinkedList<>()));

        slackNotifications.notify(ENABLE_F1);

        verify(notificationSenderMock, times(0)).send(any(Notification.class));
    }

    @Test
    void shouldSendOneNotificationToChannels() {
        UserProvider userProvider = new SingleUserProvider("someName");
        NotificationComposer composer = new NotificationComposer(configureChannels(developers), userProvider);
        NotificationSender notificationSenderMock = mock(NotificationSender.class);

        SlackNotifications slackNotifications = new SlackNotifications(composer, notificationSenderMock, new ChannelsProvider(developers));

        slackNotifications.notify(ENABLE_F1);

        verify(notificationSenderMock, times(1)).send(any(Notification.class));
    }

    @Test
    void shouldSendTwoMessagesNotificationToChannels() {
        UserProvider userProvider = new SingleUserProvider("someName");
        NotificationComposer composer = new NotificationComposer(configureChannels(developersQaEngineers), userProvider);
        NotificationSender notificationSenderMock = mock(NotificationSender.class);
        SlackNotifications slackNotifications = new SlackNotifications(composer, notificationSenderMock, new ChannelsProvider(developersQaEngineers));

        slackNotifications.notify(ENABLE_F1);

        verify(notificationSenderMock, times(2)).send(any(Notification.class));
    }
}
