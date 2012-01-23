package de.chkal.togglz.console.handlers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.chkal.togglz.console.RequestHandler;

public class InitialRedirectHandler implements RequestHandler {

    @Override
    public boolean handles(String path) {
        return path.endsWith("/");
    }

    @Override
    public void process(HttpServletRequest request, HttpServletResponse response) throws IOException {

        StringBuilder url = new StringBuilder();
        url.append(request.getRequestURI());
        if (!url.toString().endsWith("/")) {
            url.append("/");
        }
        url.append("index");
        response.sendRedirect(url.toString());
    }

}
