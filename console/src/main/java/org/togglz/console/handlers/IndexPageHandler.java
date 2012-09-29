package org.togglz.console.handlers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.togglz.console.RequestEvent;
import org.togglz.console.RequestHandlerBase;
import org.togglz.core.Feature;
import org.togglz.core.FeatureMetaData;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.group.FeatureGroup;
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

    public static class IndexPageTabView {

        private final IndexPageTab allTab;

        private final Map<String, IndexPageTab> tabMap = new HashMap<String, IndexPageTab>();

        private final List<IndexPageTab> tabs = new ArrayList<IndexPageHandler.IndexPageTab>();

        private int nextIndex = 0;

        public IndexPageTabView() {
            allTab = IndexPageTab.allTab(nextIndex++);
            tabs.add(allTab);
        }

        public void add(Feature feature, FeatureState featureState) {

            // all features are shown in the ALL tab
            IndexPageRow row = new IndexPageRow(featureState);
            allTab.add(row);

            FeatureMetaData metaData = FeatureMetaData.build(feature);
            for (FeatureGroup group : metaData.getGroups()) {

                String label = group.getLabel();
                IndexPageTab tab = tabMap.get(label);
                if (tab == null) {
                    tab = IndexPageTab.groupTab(nextIndex++, label);
                    tabMap.put(label, tab);
                    tabs.add(tab);
                }
                tab.add(row);

            }

            Collections.sort(tabs);

        }

        public List<IndexPageTab> getTabs() {
            return tabs;
        }

    }

    public static class IndexPageTab implements Comparable<IndexPageTab> {

        private final int index;
        private final List<IndexPageRow> rows = new ArrayList<IndexPageHandler.IndexPageRow>();
        private final String label;

        private IndexPageTab(int index, String label) {
            this.index = index;
            this.label = label;
        }

        private static IndexPageTab allTab(int index) {
            return new IndexPageTab(index, null);
        }

        private static IndexPageTab groupTab(int index, String label) {
            return new IndexPageTab(index, label);
        }

        @Override
        public int compareTo(IndexPageTab o) {
            return (label != null ? label : "").compareTo(o.label != null ? o.label : "");
        }

        public void add(IndexPageRow row) {
            rows.add(row);
        }

        public List<IndexPageRow> getRows() {
            return rows;
        }

        public String getLabel() {
            return label;
        }

        public int getIndex() {
            return index;
        }

        public boolean isAllTab() {
            return index == 0;
        }

    }

    public static class IndexPageRow {

        private final String name;

        private final String label;

        private final boolean enabled;

        private final String users;

        public IndexPageRow(FeatureState state) {
            this.name = state.getFeature().name();
            this.label = FeatureMetaData.build(state.getFeature()).getLabel();
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
