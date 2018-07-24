package org.togglz.servlet.test.util;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.togglz.servlet.TogglzFilter;
import org.togglz.servlet.util.HttpServletRequestHolder;

@WebServlet(urlPatterns = HttpServletRequestHolderServlet.URL_PATTERN)
public class HttpServletRequestHolderServlet extends HttpServlet {

    public static final String URL_PATTERN = "/HttpServletRequestHolderServlet";

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest ignoreMe, HttpServletResponse resp) throws ServletException, IOException {

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
