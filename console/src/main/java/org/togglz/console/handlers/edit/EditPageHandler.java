package org.togglz.console.handlers.edit;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.togglz.console.RequestEvent;
import org.togglz.console.RequestHandlerBase;
import org.togglz.console.model.FeatureModel;
import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.util.Lists;

import com.floreysoft.jmte.Engine;

public class EditPageHandler extends RequestHandlerBase {

    @Override
    public boolean handles(String path) {
        return path.equals("/edit");
    }

    @Override
    public void process(RequestEvent event) throws IOException {

        FeatureManager featureManager = event.getFeatureManager();
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

        FeatureMetaData metadata = featureManager.getMetaData(feature);
        List<ActivationStrategy> impls = Lists.asList(ServiceLoader.load(ActivationStrategy.class).iterator());
        FeatureModel featureModel = new FeatureModel(feature, metadata, impls);

        // GET requests for this feature
        if ("GET".equals(request.getMethod())) {

            FeatureState state = featureManager.getFeatureState(feature);
            featureModel.populateFromFeatureState(state);

            renderEditPage(event, featureModel);

        }

        // POST requests for this feature
        if ("POST".equals(request.getMethod())) {

            featureModel.restoreFromRequest(request);

            // no validation errors
            if (featureModel.isValid()) {

                FeatureState state = featureModel.toFeatureState();
                featureManager.setFeatureState(state);
                response.sendRedirect("index");

            }

            // got validation errors
            else {
                renderEditPage(event, featureModel);
            }

        }

    }

    private void renderEditPage(RequestEvent event, FeatureModel featureModel) throws IOException {

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("model", featureModel);

        String template = getResourceAsString("edit.html");
        String content = new Engine().transform(template, model);
        writeResponse(event, content);

    }

}
