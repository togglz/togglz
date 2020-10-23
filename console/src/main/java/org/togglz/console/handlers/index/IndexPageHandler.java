package org.togglz.console.handlers.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.togglz.console.RequestEvent;
import org.togglz.console.RequestHandlerBase;
import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.util.Services;
import org.togglz.servlet.spi.CSRFToken;
import org.togglz.servlet.spi.CSRFTokenProvider;

import com.floreysoft.jmte.Engine;

public class IndexPageHandler extends RequestHandlerBase {

    @Override
    public boolean handles(String path) {
        return path.equals("/index");
    }

    @Override
    public boolean adminOnly() {
        return true;
    }

    @Override
    public void process(RequestEvent event) throws IOException {

        FeatureManager featureManager = event.getFeatureManager();

        List<ActivationStrategy> strategies = featureManager.getActivationStrategies();

        IndexPageTabView tabView = new IndexPageTabView(strategies);

        for (Feature feature : featureManager.getFeatures()) {
            FeatureMetaData metadata = featureManager.getMetaData(feature);
            FeatureState featureState = featureManager.getFeatureState(feature);
            tabView.add(feature, metadata, featureState);
        }

        List<CSRFToken> tokens = new ArrayList<>();
        for (CSRFTokenProvider provider : Services.get(CSRFTokenProvider.class)) {
            CSRFToken token = provider.getToken(event.getRequest());
            if (token != null) {
                tokens.add(token);
            }
        }

        Map<String, Object> model = new HashMap<>();
        model.put("tokens", tokens);
        model.put("tabView", tabView);

        String template = getResourceAsString("index.html");
        String content = new Engine().transform(template, model);
        writeResponse(event, content);
    }
}
