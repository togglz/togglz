package org.togglz.servlet.test.util;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.togglz.servlet.TogglzFilter;
import org.togglz.servlet.util.HttpServletRequestHolder;

@WebServlet(urlPatterns = HttpServletRequestHolderServlet.URL_PATTERN)
public class HttpServletRequestHolderServlet extends HttpServlet {

    public static final String URL_PATTERN = "/HttpServletRequestHolderServlet";

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest ignoreMe, HttpServletResponse resp) throws IOException {

        HttpServletRequest request = HttpServletRequestHolder.get();

        if (request != null) {

            // send back the query string which the test will verify
            resp.getOutputStream().print("Query: " + request.getQueryString());
            resp.getOutputStream().print("Executed: " + request.getAttribute(TogglzFilter.EXECUTED));
            return;

        }
        resp.sendError(404);
    }

}
