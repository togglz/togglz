package org.togglz.console.handlers.edit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.togglz.console.RequestEvent;
import org.togglz.console.RequestHandlerBase;
import org.togglz.servlet.spi.CSRFToken;
import org.togglz.servlet.spi.CSRFTokenProvider;
import org.togglz.console.model.FeatureModel;
import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;

import com.floreysoft.jmte.Engine;
import org.togglz.core.util.Services;
import org.togglz.servlet.spi.CSRFTokenValidator;

public class EditPageHandler extends RequestHandlerBase {

    @Override
    public boolean handles(String path) {
        return path.equals("/edit");
    }

    @Override
    public boolean adminOnly() {
        return true;
    }

    @Override
    public void process(RequestEvent event) throws IOException {
        FeatureManager featureManager = event.getFeatureManager();
        HttpServletRequest request = event.getRequest();
        HttpServletResponse response = event.getResponse();
		if(!validateCSRFToken(event)) {
			renderErrorPage(event);
			return;
		}
        // identify the feature
        Feature feature = null;
        String featureAsString = request.getParameter("f");
        for (Feature f : featureManager.getFeatures()) {
            if (f.name().equals(featureAsString)) {
                feature = f;
            }
        }
        if (feature == null) {
            response.sendError(400);
            return;
        }

        FeatureMetaData metadata = featureManager.getMetaData(feature);
        List<ActivationStrategy> impls = featureManager.getActivationStrategies();
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

    private boolean validateCSRFToken(RequestEvent event) {
        boolean isValid = false;
        if (event.getRequestContext().isValidateCSRFToken()) {
            for (CSRFTokenValidator validator : Services.get(CSRFTokenValidator.class)) {
                if (validator.isTokenValid(event.getRequest())) {
                    isValid = true;
                    break;
                }
            }
        } else {
            isValid = true;
        }
        return isValid;
    }

    private void renderErrorPage(RequestEvent event) throws IOException {
		String template = getResourceAsString("error.html");
		String content = new Engine().transform(template, new HashMap<>());
		event.getResponse().setStatus(401);
		writeResponse(event, content);
	}

    private void renderEditPage(RequestEvent event, FeatureModel featureModel) throws IOException {
        List<CSRFToken> tokens = new ArrayList<>();
        for (CSRFTokenProvider provider : Services.get(CSRFTokenProvider.class)) {
            CSRFToken token = provider.getToken(event.getRequest());
            if (token != null) {
                tokens.add(token);
            }
        }
        Map<String, Object> model = new HashMap<>();
        model.put("model", featureModel);
        model.put("tokens", tokens);

        String template = getResourceAsString("edit.html");
        String content = new Engine().transform(template, model);
        writeResponse(event, content);
    }
}
