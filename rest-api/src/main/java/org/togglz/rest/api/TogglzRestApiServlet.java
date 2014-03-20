package org.togglz.rest.api;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.LazyResolvingFeatureManager;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.util.Strings;

public class TogglzRestApiServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected ServletContext servletContext;

    protected FeatureManager featureManager;
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        featureManager = new LazyResolvingFeatureManager();
        servletContext = config.getServletContext();
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String prefix = req.getContextPath() + req.getServletPath();
        String path = req.getRequestURI().substring(prefix.length());
        if( Strings.isBlank(path) || "/".equals(path) ) {
            resp.getWriter().write(features().toJSONString());
            resp.addHeader("Content-Type", "application/json");
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            String featureName = path.substring(1);
            JSONObject featureJson = feature(featureName);
            if (featureJson != null) {
                resp.getWriter().write(featureJson.toJSONString());
                resp.addHeader("Content-Type", "application/json");
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
    
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if(!req.getContentType().startsWith("application/json")) {
            resp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, req.getContentType());
        }
        JSONObject value = (JSONObject) JSONValue.parse(req.getReader());
        String featureName = (String) value.get("name");
        for(Feature f: featureManager.getFeatures()) {
            if( f.name().equals(featureName)) {
                FeatureState state = featureManager.getFeatureState(f);
                Boolean enabled = (Boolean) value.get("enabled");
                state.setEnabled(enabled);
                resp.setStatus(HttpServletResponse.SC_OK);
                return;
            }
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private JSONObject feature(String featureName) {
        for(Feature f: featureManager.getFeatures()) {
            if( f.name().equals(featureName)) {
                FeatureState state = featureManager.getFeatureState(f);
                return feature(f, state);
            }
        }
        return null;
    }

    private JSONArray features() {
        JSONArray features = new JSONArray(); 
        for(Feature f: featureManager.getFeatures()) {
            FeatureState state = featureManager.getFeatureState(f);
            JSONObject obj = feature(f, state);
            features.add(obj);
        }
        return features;
    }

    private JSONObject feature(Feature feature, FeatureState featureState) {
        JSONObject obj=new JSONObject();
        obj.put("name",feature.name());
        obj.put("enabled",featureState.isEnabled());
        
        if(featureState.getStrategyId() != null) {
            obj.put("strategy",strategy(featureState));
        }
        
        return obj;
    }

    private JSONObject strategy(FeatureState state) {
        JSONObject obj=new JSONObject();
        obj.put("id",state.getStrategyId());
        for(String param : state.getParameterNames()) {
            obj.put(param,state.getParameter(param));
        }
        return obj;
    }

}
