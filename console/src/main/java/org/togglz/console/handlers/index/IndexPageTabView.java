package org.togglz.console.handlers.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.togglz.console.model.FeatureModel;
import org.togglz.core.Feature;
import org.togglz.core.metadata.FeatureGroup;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;

public class IndexPageTabView {

    private final List<ActivationStrategy> strategies;

    private final IndexPageTab allTab;

    private final Map<String, IndexPageTab> tabMap = new HashMap<>();

    private final List<IndexPageTab> tabs = new ArrayList<>();

    private int nextIndex = 0;

    public IndexPageTabView(List<ActivationStrategy> strategies) {
        this.strategies = strategies;
        allTab = IndexPageTab.allTab(nextIndex++);
        tabs.add(allTab);
    }

    public void add(Feature feature, FeatureMetaData metadata, FeatureState featureState) {
        // all features are shown in the ALL tab
        FeatureModel row = new FeatureModel(feature, metadata, strategies);
        row.populateFromFeatureState(featureState);
        allTab.add(row);

        for (FeatureGroup group : metadata.getGroups()) {
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