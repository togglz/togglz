package org.togglz.slack;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.togglz.core.user.SingleUserProvider;
import org.togglz.slack.config.NotificationConfiguration;

import static org.togglz.FeatureFixture.ENABLE_F1;

@ExtendWith(MockServerExtension.class)
class SlackNotificationsIntegrationTest {

    @Test
    void shouldSendJsonToSlack(MockServerClient server) {
        server.when(HttpRequest.request("/slack"))
                .respond(HttpResponse.response().withStatusCode(200));

        NotificationConfiguration config = NotificationConfigurationFixture.configureNonAsync("http://localhost:" + server.getPort() + "/slack");
        SlackNotifications slackStateRepository = new SlackNotifications(config, new SingleUserProvider("someName"));

        slackStateRepository.notify(ENABLE_F1);

        server.verify(HttpRequest.request("/slack"));
    }
}
