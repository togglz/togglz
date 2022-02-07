package org.togglz.slack.notification;

import org.junit.Test;
import org.togglz.core.user.SingleUserProvider;
import org.togglz.core.user.UserProvider;
import org.togglz.slack.NotificationConfigurationFixture;
import org.togglz.slack.config.NotificationConfiguration;

import static org.junit.Assert.*;
import static org.togglz.FeatureFixture.DISABLE_F1;
import static org.togglz.FeatureFixture.ENABLE_F1;
import static org.togglz.FeatureFixture.ENABLE_F2;

public class NotificationComposerTest {

    static final NotificationConfiguration MINIMUM = NotificationConfigurationFixture.configureMinimum();
    static final NotificationConfiguration CUSTOM = NotificationConfigurationFixture.configureEverything();
    static final UserProvider USER_PROVIDER = new SingleUserProvider("John");

    @Test
    public void shouldComposeNotificationAccordingToJoystickAndToggles() {
        NotificationComposer composer = new NotificationComposer(MINIMUM, USER_PROVIDER);
        Notification notification = composer.compose(ENABLE_F1, MINIMUM.getChannels()).get(0);

        assertEquals(MINIMUM.getAppName() + " feature toggles", notification.getUsername());
        assertEquals(":joystick:", notification.getIcon());
        assertEquals("toggles", notification.getChannel());
        assertEquals(" feature toggles", notification.getUsername());
    }

    @Test
    public void shouldComposeNotificationAccordingToFlagPlAndDevelopers() {
        NotificationComposer composer = new NotificationComposer(CUSTOM, USER_PROVIDER);
        Notification notification = composer.compose(ENABLE_F1, CUSTOM.getChannels()).get(0);

        assertEquals(CUSTOM.getAppName() + " feature toggles", notification.getUsername());
        assertEquals(":flag-pl:", notification.getIcon());
        assertEquals("developers", notification.getChannel());
        assertEquals("tests feature toggles", notification.getUsername());
    }

    @Test
    public void shouldComposeMessageForEnabled() {
        NotificationComposer composer = new NotificationComposer(MINIMUM, USER_PROVIDER);
        Notification notification = composer.compose(ENABLE_F1, MINIMUM.getChannels()).get(0);

        assertEquals(":large_blue_circle: *F1* was enabled by John ", notification.getText());
    }

    @Test
    public void shouldComposeMessageForDisabled() {
        NotificationComposer composer = new NotificationComposer(MINIMUM, USER_PROVIDER);
        Notification notification = composer.compose(DISABLE_F1, MINIMUM.getChannels()).get(0);

        assertEquals(":white_circle: *F1* was disabled by John ", notification.getText());
    }

    @Test
    public void name() {
        NotificationConfiguration configuration = CUSTOM;
        UserProvider userProvider = new SingleUserProvider("John");
        NotificationComposer composer = new NotificationComposer(configuration, userProvider);

        Notification notification = composer.compose(ENABLE_F1, configuration.getChannels()).get(0);

        assertEquals(":green_apple: *F1* activated (John) <http://localhost/togglz|http://localhost/togglz>\n```F1```", notification.getText());
    }

    @Test
    public void name1() {
        NotificationConfiguration configuration = CUSTOM;
        UserProvider userProvider = new SingleUserProvider("John");
        NotificationComposer composer = new NotificationComposer(configuration, userProvider);

        Notification notification = composer.compose(DISABLE_F1, configuration.getChannels()).get(0);

        assertEquals(":apple: *F1* deactivated (John) <http://localhost/togglz|http://localhost/togglz>\n```F1```", notification.getText());
    }

    @Test
    public void name2() {
        NotificationConfiguration configuration = CUSTOM;
        UserProvider userProvider = new SingleUserProvider("John.D");
        NotificationComposer composer = new NotificationComposer(configuration, userProvider);

        Notification notification = composer.compose(ENABLE_F1, configuration.getChannels()).get(0);

        assertEquals(":green_apple: *F1* activated (<@John.D>) <http://localhost/togglz|http://localhost/togglz>\n```F1```", notification.getText());
    }

    @Test
    public void name3() {
        NotificationConfiguration configuration = CUSTOM;
        UserProvider userProvider = new SingleUserProvider("system");
        NotificationComposer composer = new NotificationComposer(configuration, userProvider);

        Notification notification = composer.compose(ENABLE_F1, configuration.getChannels()).get(0);

        assertEquals(":green_apple: *F1* activated (system) <http://localhost/togglz|http://localhost/togglz>\n```F1```", notification.getText());
    }

    @Test
    public void name4() {
        NotificationConfiguration configuration = CUSTOM;
        UserProvider userProvider = new SingleUserProvider("null");
        NotificationComposer composer = new NotificationComposer(configuration, userProvider);

        Notification notification = composer.compose(ENABLE_F1, configuration.getChannels()).get(0);

        assertEquals(":green_apple: *F1* activated (null) <http://localhost/togglz|http://localhost/togglz>\n```F1```", notification.getText());
    }

    @Test
    public void name5() {
        NotificationConfiguration configuration = CUSTOM;
        UserProvider userProvider = new SingleUserProvider("John");
        NotificationComposer composer = new NotificationComposer(configuration, userProvider);

        Notification notification = composer.compose(ENABLE_F2, configuration.getChannels()).get(0);

        assertEquals(":green_apple: *F2* activated (John) <http://localhost/togglz|http://localhost/togglz>\n```label2```", notification.getText());
    }
}
