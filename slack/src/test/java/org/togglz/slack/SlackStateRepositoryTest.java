package org.togglz.slack;

import org.junit.jupiter.api.Test;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.user.SingleUserProvider;
import org.togglz.slack.config.NotificationConfiguration;
import org.togglz.slack.notification.Notification;
import org.togglz.slack.notification.NotificationComposer;
import org.togglz.slack.sender.NotificationSender;
import org.togglz.slack.sender.Notifier;

import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.togglz.FeatureFixture.ENABLE_F1;
import static org.togglz.FeatureFixture.F1;

class SlackStateRepositoryTest {

    private static final Notification NOTIFICATION = new Notification();

    private final StateRepository stateRepositoryMock = mock(StateRepository.class);

    @Test
    void shouldReadStateFromWrapped() {
        SlackStateRepository slackStateRepository = new SlackStateRepository(stateRepositoryMock, mock(NotificationConfiguration.class));
        Notifier notifierMock = mock(Notifier.class);

        slackStateRepository.getFeatureState(F1);

        verify(stateRepositoryMock, times(1)).getFeatureState(F1);
        verify(notifierMock, times(0)).send(NOTIFICATION);
    }

    @Test
    void shouldWriteStateToWrappedAndSendNotification() {
        NotificationComposer composer = new NotificationComposer(NotificationConfiguration.builder().withSlackHookUrl("http://localhost:8080").build(), new SingleUserProvider("someName"));
        NotificationSender notificationSender = mock(NotificationSender.class);
        List<String> channel = new LinkedList<>();
        channel.add("channel");
        ChannelsProvider channelsProvider = new ChannelsProvider(channel);
        SlackNotifications slackNotifications = new SlackNotifications(composer,notificationSender,channelsProvider);

        SlackStateRepository slackStateRepository = new SlackStateRepository(stateRepositoryMock, slackNotifications);

        slackStateRepository.setFeatureState(ENABLE_F1);

        verify(stateRepositoryMock, times(1)).setFeatureState(ENABLE_F1);

        verify(notificationSender, times(1)).send(any(Notification.class));
    }
}
