package org.togglz.slack

import org.togglz.core.repository.FeatureState
import org.togglz.core.repository.StateRepository
import org.togglz.core.user.UserProvider
import org.togglz.slack.sender.Messenger
import org.togglz.slack.message.Message
import org.togglz.slack.message.MessageComposer
import spock.lang.Specification
import spock.lang.Subject

class SlackWrapperStateRepositorySpec extends Specification {

    final static Message MESSAGE = new Message(text: "text")
    final static List<String> CHANNELS = ["channel"]

    List constructorArgs = [NotificationConfigurationFixture.configuration(), Stub(UserProvider)]
    MessageComposer composer = Stub(constructorArgs: constructorArgs) {
        it.compose(_ as FeatureState, CHANNELS) >> [MESSAGE]
    }
    Messenger messenger = Mock()
    ChannelsProvider channelsProvider = new ChannelsProvider(CHANNELS)
    SlackStateRepository notifications = new SlackStateRepository(composer, messenger, channelsProvider)
    StateRepository wrapped = Mock()
    @Subject
    SlackWrapperStateRepository mainRepository = new SlackWrapperStateRepository(wrapped, notifications);

    def "should use wrapped repository and send notification"() {
        given:
            FeatureState state = new FeatureState(TestFeature.F1, true)
        when:
            mainRepository.setFeatureState(state)
        then:
            1 * wrapped.setFeatureState(state)
            1 * messenger.send(MESSAGE)
    }
}
