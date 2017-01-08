package org.togglz.slack

import org.togglz.core.repository.FeatureState
import org.togglz.core.repository.StateRepository
import org.togglz.core.user.UserProvider
import org.togglz.slack.notification.Notification
import org.togglz.slack.notification.NotificationComposer
import org.togglz.slack.sender.Notifier
import spock.lang.Specification
import spock.lang.Subject

import static NotificationConfigurationFixture.configureEverything
import static org.togglz.FeatureFixture.ENABLE_F1
import static org.togglz.FeatureFixture.F1

class SlackStateRepositorySpec extends Specification {

    static final Notification NOTIFICATION = new Notification(text: "text")
    static final List<String> CHANNELS = ["channel"]

    List constructorArgs = [configureEverything(), Stub(UserProvider)]
    NotificationComposer composer = Stub(constructorArgs: constructorArgs) {
        it.compose(_ as FeatureState, CHANNELS) >> [NOTIFICATION]
    }
    Notifier notifier = Mock()
    ChannelsProvider channelsProvider = new ChannelsProvider(CHANNELS)
    SlackNotifications notifications = new SlackNotifications(composer, notifier, channelsProvider)
    StateRepository wrapped = Mock()
    @Subject
    SlackStateRepository slackStateRepository = new SlackStateRepository(wrapped, notifications);


    def "should read state from wrapped"() {
        when:
            slackStateRepository.getFeatureState(F1)
        then:
            1 * wrapped.getFeatureState(F1)
            0 * notifier.send(_ as Notification)
    }

    def "should write state to wrapped and send notification"() {
        when:
            slackStateRepository.setFeatureState(ENABLE_F1)
        then:
            1 * wrapped.setFeatureState(ENABLE_F1)
            1 * notifier.send(NOTIFICATION)
    }
}
