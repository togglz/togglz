package org.togglz.core.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;

public class ConstructorBasedActiveFeatureMapTest {

    @Test
    public void canBootstrapViaConstructor()  {
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
		when(featureManager.isActive(feature1)).thenReturn(true);
		when(featureManager.isActive(feature2)).thenReturn(false);
		Map<String,Boolean> map = new ConstructorBasedActiveFeatureMap(featureManager);
		assertThat(map.size(), equalTo(2));
		assertThat(map.isEmpty(), is(false));
		assertThat(map.get(name1), equalTo(true));
		assertThat(map.get(name2), equalTo(false));
		assertThat(map.get("unknown"), equalTo(false));		
    }

}
