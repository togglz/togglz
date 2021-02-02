package org.togglz.console;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.togglz.core.manager.FeatureManager;

public class RequestEvent {

    private final ServletContext context;
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final String path;
    private final FeatureManager featureManager;
    private final RequestContext requestContext;

    public RequestEvent(FeatureManager featureManager, ServletContext context, HttpServletRequest request,
                        HttpServletResponse response, RequestContext requestContext) {
        this.featureManager = featureManager;
        this.context = context;
        this.request = request;
        this.response = response;
        this.requestContext = requestContext;

        // /contextPath/togglz/index -> /index
        String prefix = request.getContextPath() + request.getServletPath();
        path = request.getRequestURI()
            .substring(prefix.length())
            .replaceAll("(?i);jsessionid=[\\w\\.\\-]+", "");

    }

    public RequestContext getRequestContext() {
        return requestContext;
    }

    public ServletContext getContext() {
        return context;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public String getPath() {
        return path;
    }

    public FeatureManager getFeatureManager() {
        return featureManager;
    }

}
