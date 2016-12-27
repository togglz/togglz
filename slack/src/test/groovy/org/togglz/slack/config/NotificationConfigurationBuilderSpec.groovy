package org.togglz.slack.config

import spock.lang.Specification

class NotificationConfigurationBuilderSpec extends Specification {

    def "should require withSlackHookUrl property"() {
        when:
            NotificationConfigurationBuilder.create()
                    .build()
        then:
            Exception e = thrown(NullPointerException)
            e.message.startsWith("slackHookUrl is required")
    }

    def "should only withSlackHookUrl property be required"() {
        when:
            NotificationConfiguration config = NotificationConfigurationBuilder.create()
                    .withSlackHookUrl("hook")
                    .build()
        then:
            config.slackHookUrl == "hook"
    }

    def "should build configuration from properties"() {
        when:
            NotificationConfiguration config = NotificationConfigurationBuilder.create()
                    .withSlackHookUrl("hook")
                    .withChannels(["channel"])
                    .withTogglzAdminConsoleUrl("console")
                    .withAppName("app")
                    .withMessageFormat("format")
                    .build()
        then:
            config.slackHookUrl == "hook"
            config.channels == ["channel"]
            config.togglzAdminConsoleUrl == "console"
            config.appName == "app"
            config.messageFormat == "format"
    }

}
