package org.togglz.console;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.LazyResolvingFeatureManager;
import org.togglz.core.user.FeatureUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class TogglzConsoleServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected final List<RequestHandler> handlers = new ArrayList<>();

    protected ServletContext servletContext;

    protected FeatureManager featureManager;

    protected boolean secured = true;

    @Override
    public void init(ServletConfig config) {
        featureManager = new LazyResolvingFeatureManager();
        servletContext = config.getServletContext();

        String secured = servletContext.getInitParameter("org.togglz.console.SECURED");
        if (secured != null) {
            this.secured = toBool(secured);
        }

        // build list of request handlers
        for (RequestHandler requestHandler : ServiceLoader.load(RequestHandler.class)) {
            handlers.add(requestHandler);
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        RequestEvent consoleRequest =
            new RequestEvent(featureManager, servletContext, request, response);
        String path = consoleRequest.getPath();

        RequestHandler handler = getHandlerFor(path);

        if (handler != null) {
            if (!secured || !handler.adminOnly() || isFeatureAdmin(request)) {
                handler.process(consoleRequest);
            } else {
                response.sendError(403, "You are not allowed to access the Togglz Console");
            }
            return;
        }
        response.sendError(404);
    }

    protected boolean isFeatureAdmin(HttpServletRequest request) {
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

    public boolean isSecured() {
        return secured;
    }

    public void setSecured(boolean secured) {
        this.secured = secured;
    }

    private static boolean toBool(String value) {
        if (value != null && "true".equalsIgnoreCase(value.trim())) {
            return true;
        }
        if (value != null && "false".equalsIgnoreCase(value.trim())) {
            return false;
        }
        throw new IllegalArgumentException("Not a valid boolean value: " + value);
    }
}
