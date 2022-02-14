package org.togglz.slack.config;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.togglz.FeatureFixture.DISABLE_F1;
import static org.togglz.FeatureFixture.ENABLE_F1;

import org.junit.jupiter.api.Test;

class NotificationConfigurationBuilderTest {

    private static final String HOOK_URL = "https://hooks...";

    @Test
    void shouldRequiredWithSlackHookUrlProperty() {
        try {
            NotificationConfiguration.builder().build();
        } catch (IllegalArgumentException e) {
            assertEquals("slackHookUrl is required", e.getMessage());
        }
    }

    @Test
    void shouldOnlyWithSlackHookUrlPropertyBeRequired() {
        NotificationConfiguration config = NotificationConfiguration.builder()
                .withSlackHookUrl(HOOK_URL)
                .build();

        assertEquals(HOOK_URL, config.getSlackHookUrl());
        assertFalse(config.isAsyncSenderDisabled());
        assertFalse(config.isLabelingEnabled());
    }

    @Test
    void shouldBuildComplexConfiguration() {
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

        assertEquals(HOOK_URL, config.getSlackHookUrl());
        List<String> channel = new LinkedList<>();
        channel.add("channel");
        assertEquals(channel, config.getChannels());
        assertEquals("console", config.getTogglzAdminConsoleUrl());
        assertEquals("app", config.getAppName());
        assertEquals("icon", config.getAppIcon());
        assertTrue(config.isLabelingEnabled());
        assertTrue(config.isAsyncSenderDisabled());
        assertEquals("format", config.getMessageFormat());
        assertEquals("OFF", config.getChangeVerb(DISABLE_F1));
        assertEquals("ON", config.getChangeVerb(ENABLE_F1));
        assertEquals("-1", config.getStateIcon(DISABLE_F1));
        assertEquals("+1", config.getStateIcon(ENABLE_F1));
    }
}
