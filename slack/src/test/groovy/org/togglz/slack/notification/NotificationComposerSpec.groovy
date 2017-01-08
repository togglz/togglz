package org.togglz.slack.notification

import org.togglz.core.user.SingleUserProvider
import org.togglz.core.user.UserProvider
import org.togglz.slack.NotificationConfigurationFixture
import org.togglz.slack.config.NotificationConfiguration
import spock.lang.Specification
import spock.lang.Unroll

import static NotificationConfigurationFixture.ADMIN_URL
import static org.togglz.FeatureFixture.DISABLE_F1
import static org.togglz.FeatureFixture.ENABLE_F1
import static org.togglz.FeatureFixture.ENABLE_F2

@Unroll
class NotificationComposerSpec extends Specification {

    static final NotificationConfiguration MINIMUM = NotificationConfigurationFixture.configureMinimum()
    static final NotificationConfiguration CUSTOM = NotificationConfigurationFixture.configureEverything()
    static final UserProvider USER_PROVIDER = new SingleUserProvider("John")
    static final String ADMIN_LINK = "<$ADMIN_URL|$ADMIN_URL>"

    def "should compose notification according to #appIcon and #channel"() {
        given:
            NotificationComposer composer = new NotificationComposer(configuration, USER_PROVIDER)
        when:
            Notification notification = composer.compose(ENABLE_F1, configuration.channels).first()
        then:
            notification.username == "$configuration.appName feature toggles" as String
            notification.icon == expectedIcon
            notification.channel == expectedChannel
            notification.username == expectedUsername
        where:
            configuration | expectedIcon | expectedChannel | expectedUsername
            MINIMUM       | ":joystick:" | "toggles"       | " feature toggles"
            CUSTOM        | ":flag-pl:"  | "developers"    | "tests feature toggles"
    }

    def "should compose message according to the template for #scenario (minimum configuration)"() {
        given:
            NotificationConfiguration configuration = MINIMUM
            NotificationComposer composer = new NotificationComposer(configuration, USER_PROVIDER)
        when:
            Notification notification = composer.compose(state, configuration.channels).first()
        then:
            notification.text == expectedMessage as String
        where:
            scenario   | state      | expectedMessage
            "enabled"  | ENABLE_F1  | ":large_blue_circle: *F1* was enabled by John "
            "disabled" | DISABLE_F1 | ":white_circle: *F1* was disabled by John "
    }

    def "should compose message according to the template for #scenario (custom configuration)"() {
        given:
            NotificationConfiguration configuration = CUSTOM
            UserProvider userProvider = new SingleUserProvider(user as String)
            NotificationComposer composer = new NotificationComposer(configuration, userProvider)
        when:
            Notification notification = composer.compose(state, configuration.channels).first()
        then:
            notification.text == expectedMessage as String
        where:
            scenario      | user     | state      | expectedMessage
            "enabled"     | "John"   | ENABLE_F1  | ":green_apple: *F1* activated (John) $ADMIN_LINK\n```F1```"
            "disabled"    | "John"   | DISABLE_F1 | ":apple: *F1* deactivated (John) $ADMIN_LINK\n```F1```"
            "full name"   | "John.D" | ENABLE_F1  | ":green_apple: *F1* activated (<@John.D>) $ADMIN_LINK\n```F1```"
            "system user" | "system" | ENABLE_F1  | ":green_apple: *F1* activated (system) $ADMIN_LINK\n```F1```"
            "null user"   | null     | ENABLE_F1  | ":green_apple: *F1* activated (null) $ADMIN_LINK\n```F1```"
            "empty user"  | ""       | ENABLE_F1  | ":green_apple: *F1* activated () $ADMIN_LINK\n```F1```"
            "labeling"    | "John"   | ENABLE_F2  | ":green_apple: *F2* activated (John) $ADMIN_LINK\n```label2```"
    }
}
