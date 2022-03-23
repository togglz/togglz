package org.togglz.slack.sender;

import static org.togglz.slack.notification.NotificationFixture.exampleNotification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.togglz.slack.notification.NotificationFixture;

@ExtendWith(MockServerExtension.class)
class AsyncNotifierIntegrationTest {

    @Test
    void shouldSendNotificationToSlack(MockServerClient server) throws InterruptedException {
        server.when(HttpRequest.request("/slack")
                .withMethod("POST")
                .withHeader("Content-Type", "application/json")
        ).respond(HttpResponse.response().withStatusCode(200));

        NotificationSender notifier = new AsyncNotifier("http://localhost:" + server.getPort() + "/slack");

        notifier.send(exampleNotification());

        Thread.sleep(500);

        server.verify(HttpRequest.request("/slack")
                .withBody(NotificationFixture.exampleNotificationAsJson()));
    }
}
