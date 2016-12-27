package org.togglz.slack.sender;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.togglz.core.logging.Log;
import org.togglz.core.logging.LogFactory;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;

class HttpPostRequest {

    private static final Log log = LogFactory.getLog(HttpPostRequest.class);

    private final String requestUrl;

    HttpPostRequest(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    String send(String data) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(this.requestUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Length", Integer.toString(data.getBytes().length));
            writeRequest(data, connection);
            return readResponse(connection);
        } catch (Exception e) {
            log.error(e.getMessage() , e);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void writeRequest(String body, final HttpURLConnection connection) throws IOException {
        byte[] bytes = body.getBytes();
        InputStream input = new ByteArrayInputStream(bytes);
        ByteStreams.copy(input, connection.getOutputStream());
    }

    private String readResponse(final HttpURLConnection connection) throws IOException {
        return CharStreams.toString(new InputStreamReader(connection.getInputStream(), Charsets.UTF_8));
    }
}
