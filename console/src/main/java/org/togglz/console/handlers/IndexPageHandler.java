package org.togglz.console.handlers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.togglz.console.RequestEvent;
import org.togglz.console.RequestHandlerBase;
import org.togglz.core.Feature;
import org.togglz.core.FeatureMetaData;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.util.Strings;

import com.floreysoft.jmte.Engine;

public class IndexPageHandler extends RequestHandlerBase {

    @Override
    public boolean handles(String path) {
        return path.equals("/index");
    }

    @Override
    public void process(RequestEvent event) throws IOException {

        FeatureManager featureManager = FeatureContext.getFeatureManager();

        List<IndexPageRow> features = new ArrayList<IndexPageRow>();
        for (Feature f : featureManager.getFeatures()) {
            FeatureState featureState = featureManager.getFeatureState(f);
            features.add(new IndexPageRow(featureState));
        }

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("features", features);

        String template = getResourceAsString("index.html");
        String content = new Engine().transform(template, model);
        writeResponse(event, content);

    }

    public static class IndexPageRow {

        private final String name;

        private final String label;

        private final boolean enabled;

        private final String users;

        public IndexPageRow(FeatureState state) {
            this.name = state.getFeature().name();
            this.label = new FeatureMetaData(state.getFeature()).getLabel();
            this.enabled = state.isEnabled();
            this.users = Strings.join(state.getUsers(), ", ");
        }

        public String getName() {
            return name;
        }

        public String getLabel() {
            return label;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public String getUsers() {
            return users;
        }

    }
}
