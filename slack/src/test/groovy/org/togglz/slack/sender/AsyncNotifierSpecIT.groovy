package org.togglz.slack.sender

import org.junit.Rule
import org.mockserver.client.server.MockServerClient
import org.mockserver.junit.MockServerRule
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.togglz.slack.notification.NotificationFixture
import spock.lang.Specification

import static java.util.concurrent.TimeUnit.SECONDS
import static org.awaitility.Awaitility.await
import static NotificationFixture.exampleNotification

class AsyncNotifierSpecIT extends Specification {

    @Rule
    MockServerRule serverRule = new MockServerRule(this)
    MockServerClient server

    def "should send notification to Slack"() {
        given:
            server.when(HttpRequest.request("/slack")
                    .withMethod("POST")
                    .withHeader("Content-Type", "application/json")
            ).respond(HttpResponse.response().withStatusCode(200))
        and:
            NotificationSender notifier = new AsyncNotifier("http://localhost:$serverRule.port/slack")
        when:
            notifier.send(exampleNotification())
            await().atMost(3, SECONDS).until { -> isAnyRequestRetrieved() }

        then:
            server.verify(HttpRequest.request("/slack")
                    .withBody(NotificationFixture.exampleNotificationAsJson()))
    }

    boolean isAnyRequestRetrieved() {
        server.retrieveRecordedRequests(null).length > 0
    }

}
