package org.togglz.slack;

import org.togglz.core.Feature;
import org.togglz.core.logging.Log;
import org.togglz.core.logging.LogFactory;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.user.UserProvider;
import org.togglz.slack.config.NotificationConfiguration;
import org.togglz.slack.message.Message;
import org.togglz.slack.message.MessagesComposer;
import org.togglz.slack.sender.AsyncMessenger;
import org.togglz.slack.sender.MessageSender;
import org.togglz.slack.sender.Messenger;

import java.util.List;

public class SlackStateRepository implements StateRepository {

    private static final Log log = LogFactory.getLog(SlackStateRepository.class);

    private final MessagesComposer composer;

    private final MessageSender messageSender;

    private final ChannelsProvider channelsProvider;

    public SlackStateRepository(NotificationConfiguration configuration, UserProvider userProvider) {
        this(new MessagesComposer(configuration, userProvider),
                createMessageSender(configuration),
                new ChannelsProvider(configuration.getChannels()));
    }

    private static MessageSender createMessageSender(NotificationConfiguration config) {
        String url = config.getSlackHookUrl();
        return config.isDisabledAsyncSender() ? new Messenger(url) : new AsyncMessenger(url);
    }

    public SlackStateRepository(MessagesComposer composer, MessageSender messageSender, ChannelsProvider channelsProvider) {
        this.composer = composer;
        this.messageSender = messageSender;
        this.channelsProvider = channelsProvider;
    }

    @Override
    public FeatureState getFeatureState(Feature ignored) {
        return null;
    }

    @Override
    public void setFeatureState(FeatureState featureState) {
        List<Message> messages = composeMessages(featureState);
        log.info("send " + messages);
        for (Message message : messages) {
            messageSender.send(message);
        }
    }

    private List<Message> composeMessages(FeatureState featureState) {
        return composer.compose(featureState, channelsProvider.getRecipients());
    }
}
