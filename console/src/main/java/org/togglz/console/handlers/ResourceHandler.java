package org.togglz.console.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.togglz.console.RequestEvent;
import org.togglz.console.RequestHandlerBase;

public class ResourceHandler extends RequestHandlerBase {

    private final Pattern PATTERN = Pattern.compile(".*/(\\w+)\\.(css|css\\.map|js|png|eot|svg|ttf|woff|woff2)$");

    @Override
    public boolean handles(String path) {
        return PATTERN.matcher(path).matches();
    }

    @Override
    public boolean adminOnly() {
        return false;
    }

    @Override
    public void process(RequestEvent event) throws IOException {

        HttpServletResponse response = event.getResponse();

        Matcher matcher = PATTERN.matcher(event.getRequest().getRequestURI());
        if (matcher.matches()) {

            String basename = matcher.group(1);
            String type = matcher.group(2);

            InputStream stream = loadResource(basename + "." + type);
            if (stream == null) {
                response.sendError(404);
                return;
            }

            if ("css".equals(type)) {
                response.setContentType("text/css");
            } else if ("js".equals(type)) {
                response.setContentType("text/javascript");
            } else if ("png".equals(type)) {
                response.setContentType("image/png");
            } else {
                response.setContentType("image/" + type);
            }
            copy(stream, response.getOutputStream());
        }
    }
}
