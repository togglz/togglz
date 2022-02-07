package org.togglz.slack.sender;

import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.togglz.slack.notification.NotificationFixture;

import static org.togglz.slack.notification.NotificationFixture.exampleNotification;

public class AsyncNotifierIntegrationTest {

    @Rule
    public MockServerRule serverRule = new MockServerRule(this);
    private final MockServerClient server = serverRule.getClient();

    @Test
    public void shouldSendNotificationToSlack() {
        server.when(HttpRequest.request("/slack")
                .withMethod("POST")
                .withHeader("Content-Type", "application/json")
        ).respond(HttpResponse.response().withStatusCode(200));

        NotificationSender notifier = new AsyncNotifier("http://localhost:" + serverRule.getPort() + "/slack");

        notifier.send(exampleNotification());

        server.verify(HttpRequest.request("/slack")
                .withBody(NotificationFixture.exampleNotificationAsJson()));
    }
}
