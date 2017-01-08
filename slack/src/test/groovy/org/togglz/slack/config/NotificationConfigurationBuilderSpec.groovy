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
            IllegalArgumentException e = thrown()
            e.message == "slackHookUrl is required"
    }

    def "should only withSlackHookUrl property be required"() {
        when:
            NotificationConfiguration config = NotificationConfiguration.builder()
                    .withSlackHookUrl(HOOK_URL)
                    .build()
        then:
            config.slackHookUrl == HOOK_URL
            !config.asyncSenderDisabled
            !config.labelingEnabled
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
                    .enableLabeling()
                    .build()
        then:
            with(config) {
                slackHookUrl == HOOK_URL
                channels == ["channel"]
                togglzAdminConsoleUrl == "console"
                appName == "app"
                appIcon == "icon"
                getStateIcon(ENABLE_F1) == "+1"
                getStateIcon(DISABLE_F1) == "-1"
                getChangeVerb(ENABLE_F1) == "ON"
                getChangeVerb(DISABLE_F1) == "OFF"
                messageFormat == "format"
                asyncSenderDisabled
                config.labelingEnabled
            }
    }

}
