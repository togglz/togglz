package org.togglz.testing.fallback;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.metadata.EmptyFeatureMetaData;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;

/**
 * 
 * Simple {@link FeatureManager} for unit tests that enables all features.
 * 
 * @author Christian Kaltepoth
 * 
 */
public class FallbackTestFeatureManager implements FeatureManager {

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public Set<Feature> getFeatures() {
        return Collections.emptySet();
    }

    @Override
    public FeatureMetaData getMetaData(Feature feature) {
        return new EmptyFeatureMetaData(feature);
    }

    @Override
    public boolean isActive(Feature feature) {
        return true;
    }

    @Override
    public FeatureUser getCurrentFeatureUser() {
        return null;
    }

    @Override
    public FeatureState getFeatureState(Feature feature) {
        return new FeatureState(feature, true);
    }

    @Override
    public void setFeatureState(FeatureState state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ActivationStrategy> getActivationStrategies() {
        return Collections.emptyList();
    }

}
