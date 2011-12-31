package de.chkal.togglz.servlet.ui.handlers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.chkal.togglz.core.Feature;
import de.chkal.togglz.core.FeatureMetaData;
import de.chkal.togglz.core.context.FeatureContext;
import de.chkal.togglz.core.manager.FeatureManager;
import de.chkal.togglz.servlet.ui.RequestHandlerBase;

public class EditPageHandler extends RequestHandlerBase {

    @Override
    public boolean handles(String path) {
        return "/edit".equals(path);
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

        FeatureMetaData metaData = new FeatureMetaData(feature);

        if ("GET".equals(request.getMethod())) {

            String template = getResourceAsString("edit.html");

            Map<String, String> model = new HashMap<String, String>();
            model.put("%LABEL%", metaData.getLabel());

            writeResponse(response, evaluateTemplate(template, model));

        }

    }

}
