package org.togglz.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;
import org.mockito.Matchers;
import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;

public class FeatureMapTest {

    @Test
    public void canBootstrapViaConstructor() {
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
        assertThat(map.size(), equalTo(2));
        assertThat(map.isEmpty(), is(false));
        assertThat(map.get(name1), equalTo(true));
        assertThat(map.get(name2), equalTo(false));
        assertThat(map.get("unknown"), equalTo(false));
    }

    @Test
    public void shouldReturnCorrectSize() {

        List<Feature> features = Arrays.<Feature>asList(
            new NamedFeature("f1"),
            new NamedFeature("f2")
            );

        FeatureManager featureManager = mock(FeatureManager.class);
        when(featureManager.getFeatures()).thenReturn(new HashSet<Feature>(features));

        FeatureMap map = new FeatureMap(featureManager);

        assertThat(map).hasSize(2);

    }

    @Test
    public void shouldSupportLookupByFeatureName() {

        FeatureManager featureManager = mock(FeatureManager.class);
        when(featureManager.isActive(featureNamed("test"))).thenReturn(true);

        FeatureMap map = new FeatureMap(featureManager);

        assertThat(map.get("test")).isEqualTo(true);
        assertThat(map.get("other")).isEqualTo(false);

    }

    @Test
    public void shouldSupportLookupByFeatureInstance() {

        FeatureManager featureManager = mock(FeatureManager.class);
        when(featureManager.isActive(featureNamed("test"))).thenReturn(true);

        FeatureMap map = new FeatureMap(featureManager);

        assertThat(map.get(new NamedFeature("test"))).isEqualTo(true);
        assertThat(map.get(new NamedFeature("other"))).isEqualTo(false);

    }

    private Feature featureNamed(final String name) {

        return Matchers.argThat(new BaseMatcher<Feature>() {

            @Override
            public boolean matches(Object obj) {
                if (obj instanceof Feature) {
                    return ((Feature) obj).name().equals(name);
                }
                return false;
            }

            @Override
            public void describeTo(Description desc) {
                desc.appendText("Does not match: " + name);
            }
        });

    }

}
