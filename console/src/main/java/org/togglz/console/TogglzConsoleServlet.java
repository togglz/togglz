package org.togglz.console;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.LazyResolvingFeatureManager;
import org.togglz.core.user.FeatureUser;

public class TogglzConsoleServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected final List<RequestHandler> handlers = new ArrayList<RequestHandler>();

    protected ServletContext servletContext;

    protected FeatureManager featureManager;

    @Override
    public void init(ServletConfig config) throws ServletException {

        featureManager = new LazyResolvingFeatureManager();

        servletContext = config.getServletContext();

        // build list of request handlers
        Iterator<RequestHandler> handlerIterator = ServiceLoader.load(RequestHandler.class).iterator();
        while (handlerIterator.hasNext()) {
            handlers.add((RequestHandler) handlerIterator.next());
        }

    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        RequestEvent consoleRequest =
            new RequestEvent(featureManager, servletContext, request, response);
        String path = consoleRequest.getPath();

        RequestHandler handler = getHandlerFor(path);

        if (handler != null) {
            if (!handler.adminOnly() || isFeatureAdmin()) {
                handler.process(consoleRequest);
            } else {
                response.sendError(403, "You are not allowed to access the Togglz Console");
            }
            return;
        }

        response.sendError(404);

    }

    private boolean isFeatureAdmin() {
        FeatureUser user = featureManager.getCurrentFeatureUser();
        return user != null && user.isFeatureAdmin();
    }

    private RequestHandler getHandlerFor(String path) {
        for (RequestHandler page : handlers) {
            if (page.handles(path)) {
                return page;
            }
        }
        return null;
    }

}
