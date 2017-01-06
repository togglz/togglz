package org.togglz.slack.message

import org.togglz.core.user.SingleUserProvider
import org.togglz.core.user.UserProvider
import org.togglz.slack.NotificationConfigurationFixture
import org.togglz.slack.config.NotificationConfiguration
import spock.lang.Specification
import spock.lang.Unroll

import static org.togglz.FeatureFixture.DISABLE_F1
import static org.togglz.FeatureFixture.ENABLE_F1
import static org.togglz.slack.NotificationConfigurationFixture.ADMIN_URL

@Unroll
class MessageComposerSpec extends Specification {

    static final NotificationConfiguration MINIMUM = NotificationConfigurationFixture.configureMinimum()
    static final NotificationConfiguration CUSTOM = NotificationConfigurationFixture.configureEverything()
    static final UserProvider JOHN = new SingleUserProvider("John")
    static final String ADMIN_LINK = "<$ADMIN_URL|$ADMIN_URL>"

    def "should compose message DTO according to #appIcon and #channel"() {
        given:
            MessagesComposer messageComposer = new MessagesComposer(configuration, JOHN)
        when:
            List<Message> messages = messageComposer.compose(ENABLE_F1, configuration.channels)
        then:
            messages.size() == 1
            messages.first().username == "$configuration.appName feature toggles" as String
            messages.first().icon == expectedIcon
        where:
            configuration | expectedIcon | expectedChannel
            MINIMUM       | ":joystick:" | "toggles"
            CUSTOM        | ":flag-pl:"  | "developes"
    }

    def "should compose message text according to the template for #scenario (minimum configuration)"() {
        given:
            NotificationConfiguration configuration = MINIMUM
            MessagesComposer composer = new MessagesComposer(configuration, JOHN)
        when:
            List<Message> messages = composer.compose(state, configuration.channels)
        then:
            messages.first().text == expectedMessage as String
        where:
            scenario   | state      | expectedMessage
            "enabled"  | ENABLE_F1  | ":large_blue_circle: F1 was enabled by John "
            "disabled" | DISABLE_F1 | ":white_circle: F1 was disabled by John "
    }

    def "should compose message text according to the template for #scenario (custom configuration)"() {
        given:
            NotificationConfiguration configuration = CUSTOM
            UserProvider userProvider = new SingleUserProvider(user as String)
            MessagesComposer composer = new MessagesComposer(configuration, userProvider)
        when:
            List<Message> messages = composer.compose(state, configuration.channels)
        then:
            messages.first().text == expectedMessage as String
        where:
            scenario      | user     | state      | expectedMessage
            "enabled"     | "John"   | ENABLE_F1  | ":green_apple: F1 was enabled by John $ADMIN_LINK"
            "disabled"    | "John"   | DISABLE_F1 | ":apple: F1 was disabled by John $ADMIN_LINK"
            "full name"   | "John.D" | ENABLE_F1  | ":green_apple: F1 was enabled by <@John.D> $ADMIN_LINK"
            "system user" | "system" | ENABLE_F1  | ":green_apple: F1 was enabled by system $ADMIN_LINK"
            "null user"   | null     | ENABLE_F1  | ":green_apple: F1 was enabled by null $ADMIN_LINK"
            "empty user"  | ""       | ENABLE_F1  | ":green_apple: F1 was enabled by  $ADMIN_LINK"
    }
}
