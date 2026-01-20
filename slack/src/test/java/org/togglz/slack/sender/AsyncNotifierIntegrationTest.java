package org.togglz.slack.sender;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.togglz.slack.notification.NotificationFixture;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.awaitility.Awaitility.await;
import static org.togglz.slack.notification.NotificationFixture.exampleNotification;

@WireMockTest
class AsyncNotifierIntegrationTest {

    @Test
    void shouldSendNotificationToSlack(WireMockRuntimeInfo wmRuntimeInfo) {
        stubFor(post("/slack")
                .withHeader("Content-Type", containing("application/json"))
                .willReturn(ok()));

        String url = "http://localhost:" + wmRuntimeInfo.getHttpPort() + "/slack";
        NotificationSender notifier = new AsyncNotifier(url);

        notifier.send(exampleNotification());

        await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        verify(postRequestedFor(urlEqualTo("/slack"))
                                .withRequestBody(equalToJson(NotificationFixture.exampleNotificationAsJson())))
                );
    }
}
