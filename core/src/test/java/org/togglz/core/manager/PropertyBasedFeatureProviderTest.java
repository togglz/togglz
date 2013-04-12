package org.togglz.core.manager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.togglz.core.manager.ContainsFeature.containsFeature;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.util.PropertyBasedFeature;

public class PropertyBasedFeatureProviderTest {

    @Test
    public void canLoadPropertiesFromFile() throws IOException {
    	Properties properties = loadAllProperties("/features.properties");
		PropertyBasedFeatureProvider provider = new PropertyBasedFeatureProvider(properties);
		Set<Feature> features = provider.getFeatures();
		assertThat(features, containsFeature(new PropertyBasedFeature("ID_1","ID 1;true;Group 1,Group Other")));
		assertThat(features, containsFeature(new PropertyBasedFeature("ID_2","ID 2;false;Group 2")));
    }

	private Properties loadAllProperties(String resource) throws IOException {
		Properties properties = new Properties();
		properties.load(this.getClass().getResourceAsStream(resource));
		return properties;
	}

}
