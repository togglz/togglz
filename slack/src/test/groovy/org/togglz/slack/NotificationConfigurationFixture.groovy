package org.togglz.slack

import org.togglz.slack.config.NotificationConfiguration
import org.togglz.slack.config.NotificationConfigurationBuilder

class NotificationConfigurationFixture {

    public static final String HOOK_URL = "https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXXXXXXXXXXXXXXXXXX"
    public static final String ADMIN_URL = "http://localhost/togglz"

    static NotificationConfiguration configureMinimum() {
        return NotificationConfiguration.builder()
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

    static NotificationConfiguration configureChannels(List<String> channels) {
        return exampleConfiguration(HOOK_URL)
                .disableAsyncSender()
                .withChannels(channels as String[])
                .build()
    }

    static NotificationConfigurationBuilder exampleConfiguration(String hookUrl) {
        return NotificationConfiguration.builder()
                .withSlackHookUrl(hookUrl)
                .withChannels("developers")
                .withMessageFormat('$stateIcon *$feature* $changed ($user) $link')
                .withTogglzAdminConsoleUrl(ADMIN_URL)
                .withAppName("tests")
                .withAppIcon("flag-pl")
                .withStatesIcons("green_apple", "apple")
                .withChangeVerbs("activated", "deactivated")
                .enableLabeling()
    }
}
