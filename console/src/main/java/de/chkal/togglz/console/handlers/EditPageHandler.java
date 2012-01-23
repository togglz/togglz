package de.chkal.togglz.console.handlers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.chkal.togglz.console.RequestHandlerBase;
import de.chkal.togglz.core.Feature;
import de.chkal.togglz.core.FeatureMetaData;
import de.chkal.togglz.core.context.FeatureContext;
import de.chkal.togglz.core.manager.FeatureManager;
import de.chkal.togglz.core.manager.FeatureState;
import de.chkal.togglz.core.util.Strings;

public class EditPageHandler extends RequestHandlerBase {

    @Override
    public boolean handles(String uri) {
        return uri.endsWith("/edit");
    }

    @Override
    public void process(HttpServletRequest request, HttpServletResponse response) throws IOException {

        FeatureManager featureManager = FeatureContext.getFeatureManager();

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

            String template = getResourceAsString("edit.html");

            Map<String, String> model = new HashMap<String, String>();
            model.put("%LABEL%", metaData.getLabel());
            model.put("%NAME%", feature.name());
            model.put("%CHECKED%", state.isEnabled() ? "checked=\"checked\"" : "");
            model.put("%USERS%", Strings.join(state.getUsers(), "\n"));

            writeResponse(response, evaluateTemplate(template, model));

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
