package org.togglz.rest.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.LazyResolvingFeatureManager;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.util.Strings;
import org.togglz.rest.api.model.FeatureToggle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;

/**
 * Exposes a REST API for enabling/disabling Feature Toggles. 
 * 
 * All services consumes and produces "application/json". 
 * The supported methods are:
 * 
 * GET basepath/
 * 
 * GET basepath/featureName
 * 
 * PUT basepath/featureName
 * 
 * @author fabio
 */
public class TogglzRestApiServlet extends HttpServlet {

    private static final String FORWARD_SLASH = "/";

    private static final long serialVersionUID = 1L;

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String APPLICATION_XML = "application/xml";

    protected ServletContext servletContext;
    protected FeatureManager featureManager;
    protected Map<String, RequestHandler> registry;
    
    protected RequestHandler handler = new JsonRequestHandler();
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        featureManager = new LazyResolvingFeatureManager();
        servletContext = config.getServletContext();
        registry = ImmutableMap.<String, RequestHandler>of(
            APPLICATION_JSON, new JsonRequestHandler(),
            APPLICATION_XML, new JsonRequestHandler()
        );
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String prefix = req.getContextPath() + req.getServletPath();
        String path = req.getRequestURI().substring(prefix.length());
        if( Strings.isBlank(path) || FORWARD_SLASH.equals(path) ) {
            getFeatures(resp);
        } else {
            getFeature(resp, path);
        }
    }

    private void getFeature(HttpServletResponse resp, String path) throws IOException, JsonProcessingException {
        String featureName = path.startsWith(FORWARD_SLASH) ? path.substring(1) : path;
        FeatureToggle feature = feature(featureName);
        if (feature != null) {
            resp.getWriter().write(handler.serialize(feature));
            resp.addHeader(CONTENT_TYPE, handler.contentType());
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void getFeatures(HttpServletResponse resp) throws IOException, JsonProcessingException {
        resp.getWriter().write( handler.serialize(features()));
        resp.addHeader(CONTENT_TYPE, handler.contentType());
        resp.setStatus(HttpServletResponse.SC_OK);
    }
    
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if(!req.getContentType().startsWith(APPLICATION_JSON)) {
            resp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, req.getContentType());
            return;
        }

        FeatureToggle ft = handler.desserialize(req.getReader());
        for(Feature f: featureManager.getFeatures()) {
            if( f.name().equals(ft.getName())) {
                FeatureState state = featureManager.getFeatureState(f);
                Boolean enabled = ft.getEnabled();
                state.setEnabled(enabled);
                featureManager.setFeatureState(state);
                resp.setStatus(HttpServletResponse.SC_OK);
                return;
            }
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private RequestHandler handler(HttpServletRequest req) throws ServletException {
      String acceptHeader = req.getHeader("Accept");
      RequestHandler handler = registry.get(acceptHeader) ;
      if (handler == null) {
          throw new ServletException();
      }
      return handler;
    }

    private FeatureToggle feature(String featureName) {
        for(Feature f: featureManager.getFeatures()) {
            if( f.name().equals(featureName)) {
                FeatureState state = featureManager.getFeatureState(f);
                return feature(state);
            }
        }
        return null;
    }

    private List<FeatureToggle> features() {
        List<FeatureToggle> features = new ArrayList<FeatureToggle>();
        for(Feature f: featureManager.getFeatures()) {
            FeatureState state = featureManager.getFeatureState(f);
            FeatureToggle obj = feature(state);
            features.add(obj);
        }
        return features;
    }

    private FeatureToggle feature(FeatureState featureState) {
        return FeatureToggle.from(featureState);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        notAllowed(resp);
    }
    
    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        notAllowed(resp);
    }
    
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        notAllowed(resp);
    }
    
    @Override
    protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        notAllowed(resp);
    }
    
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        notAllowed(resp);
    }
    
    private void notAllowed(HttpServletResponse resp) throws IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
}