package org.togglz.slack.config;

import java.util.LinkedList;
import java.util.List;

import static org.togglz.FeatureFixture.DISABLE_F1;
import static org.togglz.FeatureFixture.ENABLE_F1;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NotificationConfigurationBuilderTest {

    private static final String HOOK_URL = "https://hooks...";

    @Test
    public void shouldRequiredWithSlackHookUrlProperty() {
        try {
            NotificationConfiguration.builder().build();
        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("slackHookUrl is required", e.getMessage());
        }
    }

    @Test
    public void shouldOnlyWithSlackHookUrlPropertyBeRequired() {
        NotificationConfiguration config = NotificationConfiguration.builder()
                .withSlackHookUrl(HOOK_URL)
                .build();

        Assertions.assertEquals(HOOK_URL, config.getSlackHookUrl());
        Assertions.assertFalse(config.isAsyncSenderDisabled());
        Assertions.assertFalse(config.isLabelingEnabled());
    }

    @Test
    public void shouldBuildComplexConfiguration() {
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
                .build();

        Assertions.assertEquals(HOOK_URL, config.getSlackHookUrl());
        List<String> channel = new LinkedList<>();
        channel.add("channel");
        Assertions.assertEquals(channel, config.getChannels());
        Assertions.assertEquals("console", config.getTogglzAdminConsoleUrl());
        Assertions.assertEquals("app", config.getAppName());
        Assertions.assertEquals("icon", config.getAppIcon());
        Assertions.assertTrue(config.isLabelingEnabled());
        Assertions.assertTrue(config.isAsyncSenderDisabled());
        Assertions.assertEquals("format", config.getMessageFormat());
        Assertions.assertEquals("OFF", config.getChangeVerb(DISABLE_F1));
        Assertions.assertEquals("ON", config.getChangeVerb(ENABLE_F1));
        Assertions.assertEquals("-1", config.getStateIcon(DISABLE_F1));
        Assertions.assertEquals("+1", config.getStateIcon(ENABLE_F1));
    }
}
