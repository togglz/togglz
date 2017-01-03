package org.togglz.slack.sender

import org.junit.Rule
import org.mockserver.client.server.MockServerClient
import org.mockserver.junit.MockServerRule
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.togglz.slack.message.MessageFixture
import spock.lang.Specification

import static java.util.concurrent.TimeUnit.SECONDS
import static org.awaitility.Awaitility.await

class AsyncMessageSenderSpecIT extends Specification {

    @Rule
    MockServerRule serverRule = new MockServerRule(this)
    MockServerClient server

    def "should send message to Slack"() {
        given:
            server.when(HttpRequest.request("/slack")
                    .withMethod("POST")
                    .withHeader("Content-Type", "application/json")
            ).respond(HttpResponse.response().withStatusCode(200))
        and:
            AsyncMessenger messenger = new AsyncMessenger("http://localhost:$serverRule.port/slack")
        when:
            messenger.send(MessageFixture.create())
            await().atMost(3, SECONDS).until { -> isAnyRequestRetrieved() }

        then:
            server.verify(HttpRequest.request("/slack")
                    .withBody(MessageFixture.createAsJson()))
    }

    boolean isAnyRequestRetrieved() {
        server.retrieveRecordedRequests(null).length > 0
    }

}
