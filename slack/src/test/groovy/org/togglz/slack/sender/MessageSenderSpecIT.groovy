package org.togglz.slack.sender

import com.google.common.base.Charsets
import com.google.common.io.CharStreams
import org.apache.http.HttpException
import org.apache.http.HttpRequest
import org.apache.http.HttpResponse
import org.apache.http.message.BasicHttpEntityEnclosingRequest
import org.apache.http.protocol.HttpContext
import org.apache.http.protocol.HttpRequestHandler
import org.apache.http.testserver.HttpServer
import org.togglz.slack.message.MessageFixture
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicReference

import static java.util.concurrent.TimeUnit.SECONDS
import static org.awaitility.Awaitility.await
import static org.hamcrest.Matchers.notNullValue

class AsyncMessageSenderSpecIT extends Specification {

    static final String EXPECTED_JSON =
            '{"channel":"toggles","username":"togglz.slack","text":"test message","icon_emoji":":robot_face:","mrkdwn":true}'
    @Shared
    HttpServer server = new HttpServer()

    AtomicReference<String> requestBody = new AtomicReference()

    def setupSpec() {
        server.start()
    }

    def "sould send message to Slack"() {
        given:
            server.registerHandler('/slack', storeRequestBody)
            String url = "http://localhost:$server.port/slack"
        and:
            AsyncMessenger messenger = new AsyncMessenger(url)
        when:
            messenger.send(MessageFixture.create())
        and:
            await().atMost(3, SECONDS).untilAtomic(requestBody, notNullValue())
        then:
            requestBody.get() == EXPECTED_JSON
    }

    HttpRequestHandler storeRequestBody = new HttpRequestHandler() {
        @Override
        public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
            BasicHttpEntityEnclosingRequest req = (BasicHttpEntityEnclosingRequest) request
            AsyncMessageSenderSpecIT.this.requestBody.set(toString(req))
        }
    };

    private static String toString(BasicHttpEntityEnclosingRequest request) {
        InputStream inputStream = request.entity.content
        return CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8))
    }

    def cleanupSpec() {
        server.shutdown()
    }
}
