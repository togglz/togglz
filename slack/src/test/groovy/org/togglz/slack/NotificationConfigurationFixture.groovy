package org.togglz.slack

import org.togglz.slack.config.NotificationConfiguration
import org.togglz.slack.config.NotificationConfigurationBuilder

class NotificationConfigurationFixture {

    public static NotificationConfiguration configuration() {
        return NotificationConfigurationBuilder.create()
                .withSlackHookUrl("https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXXXXXXXXXXXXXXXXXX")
                .withTogglzAdminConsoleUrl("http://localhost/togglz")
                .withAppName("tests")
                .build()
    }

}
