package org.togglz.console;

import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.LazyResolvingFeatureManager;
import org.togglz.core.user.FeatureUser;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    protected boolean validateCSRFToken = true;

    @Override
    public void init(ServletConfig config) {
        featureManager = new LazyResolvingFeatureManager();
        servletContext = config.getServletContext();

        String secured = servletContext.getInitParameter("org.togglz.console.SECURED");
        if (secured != null) {
            this.secured = toBool(secured);
        }

        String validateCSRFToken = servletContext.getInitParameter("org.togglz.console.validateCSRFToken");
        if (validateCSRFToken != null) {
            this.validateCSRFToken = toBool(validateCSRFToken);
        }

        // build list of request handlers
        for (RequestHandler requestHandler : ServiceLoader.load(RequestHandler.class)) {
            handlers.add(requestHandler);
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        RequestEvent consoleRequest =
                new RequestEvent(featureManager, servletContext, request, response, new RequestContext(validateCSRFToken));
        String path = consoleRequest.getPath();

        RequestHandler handler = getHandlerFor(path);

        if (handler != null) {
            if (!secured || !handler.adminOnly() || isFeatureAdmin()) {
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

    public boolean isSecured() {
        return secured;
    }

    public void setSecured(boolean secured) {
        this.secured = secured;
    }

    public void setValidateCSRFToken(boolean validateCSRFToken) {
        this.validateCSRFToken = validateCSRFToken;
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
