package org.togglz.slack

import org.togglz.slack.config.NotificationConfiguration
import org.togglz.slack.config.NotificationConfigurationBuilder

class NotificationConfigurationFixture {

    static final String HOOK_URL = "https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXXXXXXXXXXXXXXXXXX"
    static final String ADMIN_URL = "http://localhost/togglz"

    static NotificationConfiguration configureMinimum() {
        return new NotificationConfigurationBuilder()
                .withSlackHookUrl(HOOK_URL)
                .build()
    }

    static NotificationConfiguration configureEverything() {
        return exampleConfiguration(HOOK_URL)
                .build()
    }

    static NotificationConfiguration configureNonAsync(String hookUrl) {
        return exampleConfiguration(hookUrl)
                .disableAsyncSender()
                .build()
    }

    private static NotificationConfigurationBuilder exampleConfiguration(String hookUrl) {
        return new NotificationConfigurationBuilder()
                .withSlackHookUrl(hookUrl)
                .withChannels("developers")
                .withTogglzAdminConsoleUrl(ADMIN_URL)
                .withAppName("tests")
                .withAppIcon("flag-pl")
                .withStatesIcons("green_apple", "apple")
    }
}
