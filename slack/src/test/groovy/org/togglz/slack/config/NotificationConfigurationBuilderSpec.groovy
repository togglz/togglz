package org.togglz.slack.config

import spock.lang.Specification

class NotificationConfigurationBuilderSpec extends Specification {

    static final String HOOK_URL = "https://hooks..."

    def "should require withSlackHookUrl property"() {
        when:
            NotificationConfigurationBuilder.create()
                    .build()
        then:
            Exception e = thrown(IllegalArgumentException)
            e.message.startsWith("slackHookUrl is required")
    }

    def "should only withSlackHookUrl property be required"() {
        when:
            NotificationConfiguration config = NotificationConfigurationBuilder.create()
                    .withSlackHookUrl(HOOK_URL)
                    .build()
        then:
            config.slackHookUrl == HOOK_URL
    }

    def "should build complex configuration"() {
        when:
            NotificationConfiguration config = NotificationConfigurationBuilder.create()
                    .withSlackHookUrl(HOOK_URL)
                    .withChannels(["channel"])
                    .withTogglzAdminConsoleUrl("console")
                    .withAppName("app")
                    .withMessageFormat("format")
                    .build()
        then:
            config.slackHookUrl == HOOK_URL
            config.channels == ["channel"]
            config.togglzAdminConsoleUrl == "console"
            config.appName == "app"
            config.messageFormat == "format"
    }

}
