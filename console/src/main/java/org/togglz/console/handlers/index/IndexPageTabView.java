package org.togglz.console.handlers.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.togglz.core.Feature;
import org.togglz.core.FeatureMetaData;
import org.togglz.core.group.FeatureGroup;
import org.togglz.core.repository.FeatureState;

public class IndexPageTabView {

    private final IndexPageTab allTab;

    private final Map<String, IndexPageTab> tabMap = new HashMap<String, IndexPageTab>();

    private final List<IndexPageTab> tabs = new ArrayList<IndexPageTab>();

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