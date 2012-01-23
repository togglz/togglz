package de.chkal.togglz.console;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TogglzConsoleServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final List<RequestHandler> handlers = new ArrayList<RequestHandler>();

    @Override
    public void init(ServletConfig config) throws ServletException {

        // build list of request handlers
        Iterator<RequestHandler> handlerIterator = ServiceLoader.load(RequestHandler.class).iterator();
        while (handlerIterator.hasNext()) {
            handlers.add((RequestHandler) handlerIterator.next());
        }

    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //  ====>    /contxtPath/togglz/index   ->    /index
        String prefix = request.getContextPath() + request.getServletPath();
        String path = request.getRequestURI().substring(prefix.length());

        for (RequestHandler page : handlers) {

            if (page.handles(path)) {
                page.process(request, response);
                return;
            }

        }

        response.sendError(404);

    }

}
