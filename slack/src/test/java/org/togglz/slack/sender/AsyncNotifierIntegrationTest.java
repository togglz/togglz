package org.togglz.slack.sender;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.togglz.slack.notification.NotificationFixture;

import static org.togglz.slack.notification.NotificationFixture.exampleNotification;

@ExtendWith(MockServerExtension.class)
public class AsyncNotifierIntegrationTest {

    @Test
    public void shouldSendNotificationToSlack(MockServerClient server) {
        server.when(HttpRequest.request("/slack")
                .withMethod("POST")
                .withHeader("Content-Type", "application/json")
        ).respond(HttpResponse.response().withStatusCode(200));

        NotificationSender notifier = new AsyncNotifier("http://localhost:" + server.getPort() + "/slack");

        notifier.send(exampleNotification());

        server.verify(HttpRequest.request("/slack")
                .withBody(NotificationFixture.exampleNotificationAsJson()));
    }
}
