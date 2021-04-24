package org.togglz.console.handlers.index;

import org.junit.jupiter.api.Test;
import org.togglz.core.Feature;
import org.togglz.core.metadata.FeatureGroup;
import org.togglz.core.metadata.enums.EnumFeatureMetaData;
import org.togglz.core.repository.FeatureState;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IndexPageTabViewTest {

    @Test
    void shouldContainOneTab() {
        IndexPageTabView indexPageTabView = new IndexPageTabView(new LinkedList<>());
        final List<IndexPageTab> tabs = indexPageTabView.getTabs();

        assertNotNull(tabs);
        assertEquals(1,tabs.size());
    }

    @Test
    void shouldAddAndSortTab() {
        IndexPageTabView indexPageTabView = new IndexPageTabView(new LinkedList<>());
        final List<IndexPageTab> tabs = indexPageTabView.getTabs();

        final Feature feature = () -> "someTestName";
        EnumFeatureMetaData featureMetaData = new EnumFeatureMetaData(feature);
        featureMetaData.getGroups().add(new FeatureGroup() {
            @Override
            public String getLabel() {
                return "testGroupLabel";
            }

            @Override
            public boolean contains(Feature feature) {
                return false;
            }
        });
        FeatureState featureState = new FeatureState(feature);
        indexPageTabView.add(feature, featureMetaData, featureState);

        assertEquals(2, tabs.size());
        assertNull(tabs.get(0).getLabel());
        assertEquals("testGroupLabel", tabs.get(1).getLabel());
    }
}