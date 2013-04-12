package org.togglz.core.manager;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.togglz.core.Feature;
import org.togglz.core.metadata.EmptyFeatureMetaData;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.spi.FeatureProvider;
import org.togglz.core.util.PropertyBasedFeature;


public class PropertyBasedFeatureProvider implements FeatureProvider {

	private final Properties properties;

	public PropertyBasedFeatureProvider(Properties properties) {
		this.properties = properties;
	}

	@Override
	public Set<Feature> getFeatures() {
		Set<Feature> features = new HashSet<Feature>();
		Enumeration<?> names = properties.propertyNames();
		while ( names.hasMoreElements() ){
			String name = (String) names.nextElement();
			features.add(new PropertyBasedFeature(name, properties.getProperty(name)));
		}
		return features;
	}

	@Override
	public FeatureMetaData getMetaData(Feature feature) {
		if ( feature instanceof PropertyBasedFeature ){
			return ((PropertyBasedFeature)feature).getMetaData();
		}
		return new EmptyFeatureMetaData(feature);
	}

}
