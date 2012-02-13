package org.togglz.console;

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

import org.togglz.core.context.FeatureContext;
import org.togglz.core.user.FeatureUser;


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

        // first check the permission
        FeatureUser user = FeatureContext.getFeatureManager().getCurrentFeatureUser();
        if (user == null || !user.isFeatureAdmin()) {
            response.sendError(403, "You are not allowed to access the Togglz Console");
            return;
        }

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
