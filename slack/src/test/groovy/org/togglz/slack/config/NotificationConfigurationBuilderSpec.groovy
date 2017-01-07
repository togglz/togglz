package org.togglz.slack.config

import spock.lang.Specification

import static org.togglz.FeatureFixture.DISABLE_F1
import static org.togglz.FeatureFixture.ENABLE_F1

class NotificationConfigurationBuilderSpec extends Specification {

    static final String HOOK_URL = "https://hooks..."

    def "should require withSlackHookUrl property"() {
        when:
            NotificationConfiguration.builder()
                    .build()
        then:
            Exception e = thrown(IllegalArgumentException)
            e.message.startsWith("slackHookUrl is required")
    }

    def "should only withSlackHookUrl property be required"() {
        when:
            NotificationConfiguration config = NotificationConfiguration.builder()
                    .withSlackHookUrl(HOOK_URL)
                    .build()
        then:
            config.slackHookUrl == HOOK_URL
            !config.disabledAsyncSender
    }

    def "should build complex configuration"() {
        when:
            NotificationConfiguration config = NotificationConfiguration.builder()
                    .withSlackHookUrl(HOOK_URL)
                    .withChannels("channel")
                    .withTogglzAdminConsoleUrl("console")
                    .withAppName("app")
                    .withAppIcon("icon")
                    .withStatesIcons("+1", "-1")
                    .withChangeVerbs("ON", "OFF")
                    .withMessageFormat("format")
                    .disableAsyncSender()
                    .build()
        then:
            config.slackHookUrl == HOOK_URL
            config.channels == ["channel"]
            config.togglzAdminConsoleUrl == "console"
            config.appName == "app"
            config.appIcon == "icon"
            config.getStateIcon(ENABLE_F1) == "+1"
            config.getStateIcon(DISABLE_F1) == "-1"
            config.getChangeVerb(ENABLE_F1) == "ON"
            config.getChangeVerb(DISABLE_F1) == "OFF"
            config.messageFormat == "format"
            config.disabledAsyncSender
    }

}
