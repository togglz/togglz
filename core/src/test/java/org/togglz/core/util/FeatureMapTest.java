package org.togglz.core.util;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FeatureMapTest {

    @Test
    void canBootstrapViaConstructor() {
        FeatureManager featureManager = mock(FeatureManager.class);
        Set<Feature> features = new HashSet<Feature>();
        String name1 = "Feature 1";
        String name2 = "Feature 2";
        Feature feature1 = mock(Feature.class, name1);
        Feature feature2 = mock(Feature.class, name2);
        features.add(feature1);
        features.add(feature2);
        when(featureManager.getFeatures()).thenReturn(features);
        when(feature1.name()).thenReturn(name1);
        when(feature2.name()).thenReturn(name2);
        when(featureManager.isActive(featureNamed(name1))).thenReturn(true);
        when(featureManager.isActive(featureNamed(name2))).thenReturn(false);
        Map<Object, Boolean> map = new FeatureMap(featureManager);
        assertEquals(2, map.size());
        assertFalse(map.isEmpty());
        assertTrue(map.get(name1));
        assertFalse(map.get(name2));
        assertFalse(map.get("unknown"));
    }

    @Test
    void shouldReturnCorrectSize() {
        List<Feature> features = Arrays.<Feature>asList(
            new NamedFeature("f1"),
            new NamedFeature("f2")
            );

        FeatureManager featureManager = mock(FeatureManager.class);
        when(featureManager.getFeatures()).thenReturn(new HashSet<Feature>(features));

        FeatureMap map = new FeatureMap(featureManager);

        assertEquals(2, map.size());
    }

    @Test
    void shouldSupportLookupByFeatureName() {
        FeatureManager featureManager = mock(FeatureManager.class);
        when(featureManager.isActive(featureNamed("test"))).thenReturn(true);

        FeatureMap map = new FeatureMap(featureManager);

        assertEquals(true, map.get("test"));
        assertEquals(false, map.get("other"));
    }

    @Test
    void shouldSupportLookupByFeatureInstance() {

        FeatureManager featureManager = mock(FeatureManager.class);
        when(featureManager.isActive(featureNamed("test"))).thenReturn(true);

        FeatureMap map = new FeatureMap(featureManager);

        assertEquals(true, map.get("test"));
        assertEquals(false, map.get("other"));
    }

    private Feature featureNamed(final String name) {
        return ArgumentMatchers.argThat(feature -> {
            if (feature != null) {
                return feature.name().equals(name);
            }
            return false;
        });

    }
}
