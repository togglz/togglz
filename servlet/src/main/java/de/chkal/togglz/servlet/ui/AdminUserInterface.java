package de.chkal.togglz.servlet.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.chkal.togglz.core.manager.FeatureManager;

public class AdminUserInterface {

    private List<RequestHandler> handlers = new ArrayList<RequestHandler>();

    private String prefix;

    public AdminUserInterface(FeatureManager featureManager, ServletContext servletContext, String dir) {

        // example: /myapp/togglez
        this.prefix = servletContext.getContextPath() + "/" + dir;

        // request handlers
        Iterator<RequestHandler> handlerIterator = ServiceLoader.load(RequestHandler.class).iterator();
        while (handlerIterator.hasNext()) {
            handlers.add((RequestHandler) handlerIterator.next());
        }

    }

    public boolean process(HttpServletRequest request, HttpServletResponse response) throws IOException {

        if (request.getRequestURI().startsWith(prefix)) {

            String path = request.getRequestURI().substring(prefix.length());

            for (RequestHandler page : handlers) {

                if (page.handles(path)) {
                    page.process(request, response);
                    return true;
                }

            }
        }

        return false;

    }

}
