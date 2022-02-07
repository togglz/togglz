package org.togglz.slack.config;

import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.togglz.FeatureFixture.DISABLE_F1;
import static org.togglz.FeatureFixture.ENABLE_F1;

public class NotificationConfigurationBuilderTest {

    private static final String HOOK_URL = "https://hooks...";

    @Test
    public void shouldRequiredWithSlackHookUrlProperty() {
        try {
            NotificationConfiguration.builder().build();
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("slackHookUrl is required", e.getMessage());
        }
    }

    @Test
    public void shouldOnlyWithSlackHookUrlPropertyBeRequired() {
        NotificationConfiguration config = NotificationConfiguration.builder()
                .withSlackHookUrl(HOOK_URL)
                .build();

        Assert.assertEquals(HOOK_URL, config.getSlackHookUrl());
        Assert.assertFalse(config.isAsyncSenderDisabled());
        Assert.assertFalse(config.isLabelingEnabled());
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

        Assert.assertEquals(HOOK_URL, config.getSlackHookUrl());
        List<String> channel = new LinkedList<>();
        channel.add("channel");
        Assert.assertEquals(channel, config.getChannels());
        Assert.assertEquals("console", config.getTogglzAdminConsoleUrl());
        Assert.assertEquals("app", config.getAppName());
        Assert.assertEquals("icon", config.getAppIcon());
        Assert.assertTrue(config.isLabelingEnabled());
        Assert.assertTrue(config.isAsyncSenderDisabled());
        Assert.assertEquals("format", config.getMessageFormat());
        Assert.assertEquals("OFF", config.getChangeVerb(DISABLE_F1));
        Assert.assertEquals("ON", config.getChangeVerb(ENABLE_F1));
        Assert.assertEquals("-1", config.getStateIcon(DISABLE_F1));
        Assert.assertEquals("+1", config.getStateIcon(ENABLE_F1));
    }
}
