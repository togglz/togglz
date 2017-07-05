package org.togglz.core.manager;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.togglz.core.Feature;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.spi.FeatureProvider;

/**
 * A feature provider that delegates to one or more other providers.
 * 
 * @author Dave Syer
 *
 */
public class CompositeFeatureProvider implements FeatureProvider {

	private final List<FeatureProvider> delegates;
	
	public CompositeFeatureProvider(List<FeatureProvider> delegates) {
		this.delegates = delegates;
	}

	public CompositeFeatureProvider(FeatureProvider... delegates) {
		this.delegates = Arrays.asList(delegates);
	}

	@Override
	public Set<Feature> getFeatures() {
		Set<Feature> features = new LinkedHashSet<>();
		for (FeatureProvider provider : delegates) {
			features.addAll(provider.getFeatures());
		}
		return features;
	}

	@Override
	public FeatureMetaData getMetaData(Feature feature) {
		for (FeatureProvider provider : delegates) {
			FeatureMetaData metaData = provider.getMetaData(feature);
			if (metaData!=null) {
				return metaData;
			}
		}
		return null;
	}

}
