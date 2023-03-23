package org.togglz.console.handlers;

import java.io.IOException;

import org.togglz.console.RequestEvent;
import org.togglz.console.RequestHandler;

public class InitialRedirectHandler implements RequestHandler {

    @Override
    public boolean handles(String path) {
        return path.equals("") || path.equals("/");
    }

    @Override
    public boolean adminOnly() {
        return false;
    }

    @Override
    public void process(RequestEvent event) throws IOException {
        StringBuilder url = new StringBuilder();
        url.append(event.getRequest().getRequestURI());
        if (!url.toString().endsWith("/")) {
            url.append("/");
        }
        url.append("index");
        event.getResponse().sendRedirect(url.toString());
    }

}
