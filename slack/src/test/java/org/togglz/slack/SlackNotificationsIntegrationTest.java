package org.togglz.slack;

import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.togglz.core.user.SingleUserProvider;
import org.togglz.slack.config.NotificationConfiguration;

import static org.togglz.FeatureFixture.ENABLE_F1;

public class SlackNotificationsIntegrationTest {

    @Rule
    public MockServerRule serverRule = new MockServerRule(this);
    private final MockServerClient server = serverRule.getClient();

    @Test
    public void shouldSendJsonToSlack() {
        server.when(HttpRequest.request("/slack"))
                .respond(HttpResponse.response().withStatusCode(200));

        NotificationConfiguration config = NotificationConfigurationFixture.configureNonAsync("http://localhost:" + serverRule.getPort() + "/slack");
        SlackNotifications slackStateRepository = new SlackNotifications(config, new SingleUserProvider("someName"));

        slackStateRepository.notify(ENABLE_F1);

        server.verify(HttpRequest.request("/slack"));
    }
}
