package org.togglz.slack.message

import org.togglz.core.repository.FeatureState
import org.togglz.core.user.UserProvider
import org.togglz.slack.UserProviderFixture
import org.togglz.slack.config.NotificationConfiguration
import org.togglz.slack.NotificationConfigurationFixture
import org.togglz.slack.TestFeature
import spock.lang.Specification
import spock.lang.Unroll

class MessageComposerSpec extends Specification {

    final static String ON = ":large_blue_circle:"
    final static String OFF = ":white_circle:"

    @Unroll
    def "should compose message according to the template for #scenario"() {
        given:
            NotificationConfiguration config = NotificationConfigurationFixture.configuration()
            UserProvider userProvider = UserProviderFixture.withUser(user)
            MessageComposer messageComposer = new MessageComposer(config, userProvider)
        and:
            FeatureState featureState = new FeatureState(TestFeature.F1, state)
        when:
            List<Message> messages = messageComposer.compose(featureState, config.channels)
        then:
            messages.size() == 1
            Message msg = messages.first()
            msg.text.contains(expectedMessagePart)
            msg.text.contains(" <$config.togglzAdminConsoleUrl|$config.togglzAdminConsoleUrl>")
            msg.username == "$config.appName feature toggles" as String
            msg.icon == icon
            msg.channel == "toggles"
        where:
            scenario          | user     | state | expectedMessagePart           | icon
            "full name"       | "John.D" | true  | "F1 was enabled by <@John.D>" | ON
            "enabled toggle"  | "John"   | true  | "F1 was enabled by John"      | ON
            "disabled toggle" | "John"   | false | "F1 was disabled by John"     | OFF
            "null user"       | null     | true  | "F1 was enabled by null"      | ON
            "empty user"      | ""       | true  | "F1 was enabled by "          | ON
            "system user"     | "system" | true  | "F1 was enabled by system"    | ON
    }
}
