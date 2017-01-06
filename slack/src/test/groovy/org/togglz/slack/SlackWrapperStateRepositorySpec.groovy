package org.togglz.slack

import org.togglz.core.repository.FeatureState
import org.togglz.core.repository.StateRepository
import org.togglz.core.user.UserProvider
import org.togglz.slack.message.Message
import org.togglz.slack.message.MessagesComposer
import org.togglz.slack.sender.Messenger
import spock.lang.Specification
import spock.lang.Subject

import static org.togglz.FeatureFixture.ENABLE_F1
import static org.togglz.slack.NotificationConfigurationFixture.configureEverything

class SlackWrapperStateRepositorySpec extends Specification {

    static final Message MESSAGE = new Message(text: "text")
    static final List<String> CHANNELS = ["channel"]

    List constructorArgs = [configureEverything(), Stub(UserProvider)]
    MessagesComposer composer = Stub(constructorArgs: constructorArgs) {
        it.compose(_ as FeatureState, CHANNELS) >> [MESSAGE]
    }
    Messenger messenger = Mock()
    ChannelsProvider channelsProvider = new ChannelsProvider(CHANNELS)
    SlackStateRepository notifications = new SlackStateRepository(composer, messenger, channelsProvider)
    StateRepository wrapped = Mock()
    @Subject
    SlackWrapperStateRepository mainRepository = new SlackWrapperStateRepository(wrapped, notifications);

    def "should use wrapped repository and send notification"() {
        when:
            mainRepository.setFeatureState(ENABLE_F1)
        then:
            1 * wrapped.setFeatureState(ENABLE_F1)
            1 * messenger.send(MESSAGE)
    }
}
