package org.togglz.core.manager;

import java.util.Collections;
import java.util.Set;

import org.togglz.core.Feature;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.spi.FeatureProvider;

/**
 * Dummy implementation of {@link FeatureProvider} that has no features.
 *
 * @author Marcel Overdijk
 */
public class EmptyFeatureProvider implements FeatureProvider {

    @Override
    public Set<Feature> getFeatures() {
        return Collections.emptySet();
    }

    @Override
    public FeatureMetaData getMetaData(Feature feature) {
        return null;
    }
}
