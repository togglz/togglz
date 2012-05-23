package org.togglz.console;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestEvent {

    private final ServletContext context;
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final String path;

    public RequestEvent(ServletContext context, HttpServletRequest request, HttpServletResponse response) {
        this.context = context;
        this.request = request;
        this.response = response;

        // /contextPath/togglz/index -> /index
        String prefix = request.getContextPath() + request.getServletPath();
        path = request.getRequestURI().substring(prefix.length());

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

}
