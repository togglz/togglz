package org.togglz.slack

import org.togglz.core.user.UserProvider
import org.togglz.slack.notification.Notification
import org.togglz.slack.notification.NotificationComposer
import org.togglz.slack.sender.NotificationSender
import spock.lang.Specification
import spock.lang.Unroll

import static org.togglz.FeatureFixture.ENABLE_F1
import static org.togglz.slack.NotificationConfigurationFixture.configureChannels

@Unroll
class SlackNotificationsSpec extends Specification {

    def "should send #numerOfMessages notification to #channels"() {
        given:
            UserProvider userProvider = Stub()
            NotificationComposer composer = new NotificationComposer(configureChannels(channels), userProvider)
            NotificationSender sender = Mock()
            SlackNotifications slackNotifications = new SlackNotifications(composer, sender, new ChannelsProvider(channels))
        when:
            slackNotifications.notify(ENABLE_F1)
        then:
            numerOfMessages * sender.send(_ as Notification)
        where:
            channels                       | _
            []                             | _
            ["developers"]                 | _
            ["developers", "qa-engineers"] | _
            numerOfMessages = channels.size()
    }

}
