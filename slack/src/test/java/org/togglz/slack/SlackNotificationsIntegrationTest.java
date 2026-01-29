package org.togglz.slack;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.togglz.core.user.SingleUserProvider;
import org.togglz.slack.config.NotificationConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.togglz.FeatureFixture.ENABLE_F1;

@WireMockTest
class SlackNotificationsIntegrationTest {

    @Test
    void shouldSendJsonToSlack(WireMockRuntimeInfo wmRuntimeInfo) {
        stubFor(post("/slack")
                .willReturn(ok()));

        String url = "http://localhost:" + wmRuntimeInfo.getHttpPort() + "/slack";
        NotificationConfiguration config = NotificationConfigurationFixture.configureNonAsync(url);

        SlackNotifications slackNotifications = new SlackNotifications(
                config,
                new SingleUserProvider("someName")
        );

        slackNotifications.notify(ENABLE_F1);

        verify(postRequestedFor(urlEqualTo("/slack"))
                .withRequestBody(matchingJsonPath("$.text"))
                .withHeader("Content-Type", containing("application/json")));
    }
}
