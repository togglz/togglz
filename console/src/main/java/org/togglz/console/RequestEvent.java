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

    public RequestEvent(FeatureManager featureManager, ServletContext context, HttpServletRequest request,
        HttpServletResponse response) {
        this.featureManager = featureManager;
        this.context = context;
        this.request = request;
        this.response = response;

        // /contextPath/togglz/index -> /index
        String prefix = request.getContextPath() + request.getServletPath();
        path = request.getRequestURI()
            .substring(prefix.length())
            .replaceAll("(?i);jsessionid=[\\w\\.\\-]+", "");

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
