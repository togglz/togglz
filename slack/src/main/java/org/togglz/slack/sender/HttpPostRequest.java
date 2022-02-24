package org.togglz.slack.sender;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.util.IOUtils;

class HttpPostRequest {

    private static final Logger log = LoggerFactory.getLogger(HttpPostRequest.class);

    private static final Long TIMEOUT = TimeUnit.SECONDS.toMillis(5);

    private final String requestUrl;

    HttpPostRequest(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    String send(byte[] body) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(this.requestUrl);
            connection = prepareConnection(url, body);
            writeRequest(body, connection);
            return readResponse(connection);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private HttpURLConnection prepareConnection(URL url, byte[] body) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Length", Integer.toString(body.length));
        connection.setConnectTimeout(TIMEOUT.intValue());
        connection.setReadTimeout(TIMEOUT.intValue());
        return connection;
    }

    private void writeRequest(byte[] body, final HttpURLConnection connection) throws IOException {
        InputStream input = new ByteArrayInputStream(body);
        IOUtils.copy(input, connection.getOutputStream());
    }

    private String readResponse(final HttpURLConnection connection) throws IOException {
        InputStream inputStream = connection.getInputStream();
        return IOUtils.toString(new InputStreamReader(inputStream, UTF_8));
    }
}
