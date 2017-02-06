package org.togglz.slack

import org.junit.Rule
import org.mockserver.client.server.MockServerClient
import org.mockserver.junit.MockServerRule
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.togglz.core.user.UserProvider
import org.togglz.slack.config.NotificationConfiguration
import spock.lang.Specification

import static org.togglz.FeatureFixture.ENABLE_F1
import static NotificationConfigurationFixture.configureNonAsync

class SlackNotificationsSpecIT extends Specification {

    @Rule
    MockServerRule serverRule = new MockServerRule(this)
    MockServerClient server

    def "should send json to Slack"() {
        given:
            server.when(HttpRequest.request("/slack"))
                    .respond(HttpResponse.response().withStatusCode(200))
        and:
            NotificationConfiguration config = configureNonAsync("http://localhost:$serverRule.port/slack")
            SlackNotifications slackStateRepository = new SlackNotifications(config, Stub(UserProvider))
        when:
            slackStateRepository.notify(ENABLE_F1)
        then:
            server.verify(HttpRequest.request("/slack"))
    }

}
