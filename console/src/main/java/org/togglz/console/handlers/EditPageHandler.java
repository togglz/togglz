package org.togglz.console.handlers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.togglz.console.RequestEvent;
import org.togglz.console.RequestHandlerBase;
import org.togglz.core.Feature;
import org.togglz.core.FeatureMetaData;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.util.Strings;

import com.floreysoft.jmte.Engine;

public class EditPageHandler extends RequestHandlerBase {

    @Override
    public boolean handles(String path) {
        return path.equals("/edit");
    }

    @Override
    public void process(RequestEvent event) throws IOException {

        FeatureManager featureManager = FeatureContext.getFeatureManager();
        HttpServletRequest request = event.getRequest();
        HttpServletResponse response = event.getResponse();

        // identify the feature
        Feature feature = null;
        String featureAsString = request.getParameter("f");
        for (Feature f : featureManager.getFeatures()) {
            if (f.name().equals(featureAsString)) {
                feature = f;
            }
        }
        if (feature == null) {
            response.sendError(403);
            return;
        }

        // we may need the meta data for this feature
        FeatureMetaData metaData = new FeatureMetaData(feature);

        // GET requests for this feature
        if ("GET".equals(request.getMethod())) {

            FeatureState state = featureManager.getFeatureState(feature);

            Map<String, Object> model = new HashMap<String, Object>();
            model.put("label", metaData.getLabel());
            model.put("name", feature.name());
            model.put("enabled", state.isEnabled());
            model.put("users", Strings.join(state.getUsers(), "\n"));

            String template = getResourceAsString("edit.html");
            String content = new Engine().transform(template, model);
            writeResponse(event, content);

        }

        // POST requests for this feature
        if ("POST".equals(request.getMethod())) {

            String enabledParam = request.getParameter("enabled");
            String usersParam = request.getParameter("users");

            boolean enabled = enabledParam != null && enabledParam.trim().length() > 0;

            List<String> users = new ArrayList<String>();
            for (String u : usersParam.split("[,\\s]+")) {
                if (u != null && u.trim().length() > 0) {
                    users.add(u.trim());
                }
            }

            FeatureState state = new FeatureState(feature, enabled, users);
            featureManager.setFeatureState(state);

            response.sendRedirect("index");

        }

    }

}
