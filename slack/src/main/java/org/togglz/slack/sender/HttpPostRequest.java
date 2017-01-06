package org.togglz.slack.sender;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import org.togglz.core.logging.Log;
import org.togglz.core.logging.LogFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class HttpPostRequest {

    private static final Log log = LogFactory.getLog(HttpPostRequest.class);

    private final String requestUrl;

    HttpPostRequest(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    String send(byte[] body) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(this.requestUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Length", Integer.toString(body.length));
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

    private void writeRequest(byte[] body, final HttpURLConnection connection) throws IOException {
        InputStream input = new ByteArrayInputStream(body);
        ByteStreams.copy(input, connection.getOutputStream());
    }

    private String readResponse(final HttpURLConnection connection) throws IOException {
        InputStream inputStream = connection.getInputStream();
        return CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8));
    }
}
