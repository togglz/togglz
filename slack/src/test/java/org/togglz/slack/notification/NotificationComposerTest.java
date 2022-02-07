package org.togglz.slack.notification;

import static org.togglz.FeatureFixture.DISABLE_F1;
import static org.togglz.FeatureFixture.ENABLE_F1;
import static org.togglz.FeatureFixture.ENABLE_F2;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.togglz.core.user.SingleUserProvider;
import org.togglz.core.user.UserProvider;
import org.togglz.slack.NotificationConfigurationFixture;
import org.togglz.slack.config.NotificationConfiguration;

public class NotificationComposerTest {

    static final NotificationConfiguration MINIMUM = NotificationConfigurationFixture.configureMinimum();
    static final NotificationConfiguration CUSTOM = NotificationConfigurationFixture.configureEverything();
    static final UserProvider USER_PROVIDER = new SingleUserProvider("John");

    @Test
    public void shouldComposeNotificationAccordingToJoystickAndToggles() {
        NotificationComposer composer = new NotificationComposer(MINIMUM, USER_PROVIDER);
        Notification notification = composer.compose(ENABLE_F1, MINIMUM.getChannels()).get(0);

        Assertions.assertEquals(MINIMUM.getAppName() + " feature toggles", notification.getUsername());
        Assertions.assertEquals(":joystick:", notification.getIcon());
        Assertions.assertEquals("toggles", notification.getChannel());
        Assertions.assertEquals(" feature toggles", notification.getUsername());
    }

    @Test
    public void shouldComposeNotificationAccordingToFlagPlAndDevelopers() {
        NotificationComposer composer = new NotificationComposer(CUSTOM, USER_PROVIDER);
        Notification notification = composer.compose(ENABLE_F1, CUSTOM.getChannels()).get(0);

        Assertions.assertEquals(CUSTOM.getAppName() + " feature toggles", notification.getUsername());
        Assertions.assertEquals(":flag-pl:", notification.getIcon());
        Assertions.assertEquals("developers", notification.getChannel());
        Assertions.assertEquals("tests feature toggles", notification.getUsername());
    }

    @Test
    public void shouldComposeMessageForEnabled() {
        NotificationComposer composer = new NotificationComposer(MINIMUM, USER_PROVIDER);
        Notification notification = composer.compose(ENABLE_F1, MINIMUM.getChannels()).get(0);

        Assertions.assertEquals(":large_blue_circle: *F1* was enabled by John ", notification.getText());
    }

    @Test
    public void shouldComposeMessageForDisabled() {
        NotificationComposer composer = new NotificationComposer(MINIMUM, USER_PROVIDER);
        Notification notification = composer.compose(DISABLE_F1, MINIMUM.getChannels()).get(0);

        Assertions.assertEquals(":white_circle: *F1* was disabled by John ", notification.getText());
    }

    @Test
    public void name() {
        NotificationConfiguration configuration = CUSTOM;
        UserProvider userProvider = new SingleUserProvider("John");
        NotificationComposer composer = new NotificationComposer(configuration, userProvider);

        Notification notification = composer.compose(ENABLE_F1, configuration.getChannels()).get(0);

        Assertions.assertEquals(":green_apple: *F1* activated (John) <http://localhost/togglz|http://localhost/togglz>\n```F1```", notification.getText());
    }

    @Test
    public void name1() {
        NotificationConfiguration configuration = CUSTOM;
        UserProvider userProvider = new SingleUserProvider("John");
        NotificationComposer composer = new NotificationComposer(configuration, userProvider);

        Notification notification = composer.compose(DISABLE_F1, configuration.getChannels()).get(0);

        Assertions.assertEquals(":apple: *F1* deactivated (John) <http://localhost/togglz|http://localhost/togglz>\n```F1```", notification.getText());
    }

    @Test
    public void name2() {
        NotificationConfiguration configuration = CUSTOM;
        UserProvider userProvider = new SingleUserProvider("John.D");
        NotificationComposer composer = new NotificationComposer(configuration, userProvider);

        Notification notification = composer.compose(ENABLE_F1, configuration.getChannels()).get(0);

        Assertions.assertEquals(":green_apple: *F1* activated (<@John.D>) <http://localhost/togglz|http://localhost/togglz>\n```F1```", notification.getText());
    }

    @Test
    public void name3() {
        NotificationConfiguration configuration = CUSTOM;
        UserProvider userProvider = new SingleUserProvider("system");
        NotificationComposer composer = new NotificationComposer(configuration, userProvider);

        Notification notification = composer.compose(ENABLE_F1, configuration.getChannels()).get(0);

        Assertions.assertEquals(":green_apple: *F1* activated (system) <http://localhost/togglz|http://localhost/togglz>\n```F1```", notification.getText());
    }

    @Test
    public void name4() {
        NotificationConfiguration configuration = CUSTOM;
        UserProvider userProvider = new SingleUserProvider("null");
        NotificationComposer composer = new NotificationComposer(configuration, userProvider);

        Notification notification = composer.compose(ENABLE_F1, configuration.getChannels()).get(0);

        Assertions.assertEquals(":green_apple: *F1* activated (null) <http://localhost/togglz|http://localhost/togglz>\n```F1```", notification.getText());
    }

    @Test
    public void name5() {
        NotificationConfiguration configuration = CUSTOM;
        UserProvider userProvider = new SingleUserProvider("John");
        NotificationComposer composer = new NotificationComposer(configuration, userProvider);

        Notification notification = composer.compose(ENABLE_F2, configuration.getChannels()).get(0);

        Assertions.assertEquals(":green_apple: *F2* activated (John) <http://localhost/togglz|http://localhost/togglz>\n```label2```", notification.getText());
    }
}
