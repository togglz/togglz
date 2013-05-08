package org.togglz.console.handlers.index;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.togglz.console.RequestEvent;
import org.togglz.console.RequestHandlerBase;
import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.util.Lists;

import com.floreysoft.jmte.Engine;

public class IndexPageHandler extends RequestHandlerBase {

    @Override
    public boolean handles(String path) {
        return path.equals("/index");
    }

    @Override
    public void process(RequestEvent event) throws IOException {

        FeatureManager featureManager = event.getFeatureManager();

        List<ActivationStrategy> strategies = Lists.asList(ServiceLoader.load(ActivationStrategy.class).iterator());

        IndexPageTabView tabView = new IndexPageTabView(strategies);

        for (Feature feature : featureManager.getFeatures()) {
            FeatureMetaData metadata = featureManager.getMetaData(feature);
            FeatureState featureState = featureManager.getFeatureState(feature);
            tabView.add(feature, metadata, featureState);
        }

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("tabView", tabView);

        String template = getResourceAsString("index.html");
        String content = new Engine().transform(template, model);
        writeResponse(event, content);

    }
}
