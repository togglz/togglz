package org.togglz.slack

import org.togglz.core.repository.FeatureState
import org.togglz.core.repository.StateRepository
import org.togglz.core.user.UserProvider
import org.togglz.slack.notification.Notification
import org.togglz.slack.notification.NotificationComposer
import org.togglz.slack.sender.Notifier
import spock.lang.Specification
import spock.lang.Subject

import static org.togglz.FeatureFixture.ENABLE_F1
import static NotificationConfigurationFixture.configureEverything

class SlackWrapperStateRepositorySpec extends Specification {

    static final Notification NOTIFICATION = new Notification(text: "text")
    static final List<String> CHANNELS = ["channel"]

    List constructorArgs = [configureEverything(), Stub(UserProvider)]
    NotificationComposer composer = Stub(constructorArgs: constructorArgs) {
        it.compose(_ as FeatureState, CHANNELS) >> [NOTIFICATION]
    }
    Notifier messenger = Mock()
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
            1 * messenger.send(NOTIFICATION)
    }
}
