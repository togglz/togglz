package org.togglz.console.handlers.index;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.togglz.console.RequestEvent;
import org.togglz.console.RequestHandlerBase;
import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.FeatureState;

import com.floreysoft.jmte.Engine;

public class IndexPageHandler extends RequestHandlerBase {

    @Override
    public boolean handles(String path) {
        return path.equals("/index");
    }

    @Override
    public void process(RequestEvent event) throws IOException {

        FeatureManager featureManager = FeatureContext.getFeatureManager();

        IndexPageTabView tabView = new IndexPageTabView();

        for (Feature feature : featureManager.getFeatures()) {
            FeatureState featureState = featureManager.getFeatureState(feature);
            tabView.add(feature, featureState);
        }

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("tabView", tabView);

        String template = getResourceAsString("index.html");
        String content = new Engine().transform(template, model);
        writeResponse(event, content);

    }
}
