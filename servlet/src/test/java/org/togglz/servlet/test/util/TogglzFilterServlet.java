package org.togglz.servlet.test.util;

import org.togglz.servlet.TogglzFilter;
import org.togglz.servlet.util.HttpServletRequestHolder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = TogglzFilterServlet.URL_PATTERN)
public class TogglzFilterServlet extends HttpServlet {

    public static final String URL_PATTERN = "/TogglzFilterServlet";

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest ignoreMe, HttpServletResponse resp) throws ServletException, IOException {

        HttpServletRequest request = HttpServletRequestHolder.get();

        if (request != null) {

            resp.getOutputStream().print("executed=" + request.getAttribute(TogglzFilter.EXECUTED));
            return;

        }
        resp.sendError(404);

    }

}
