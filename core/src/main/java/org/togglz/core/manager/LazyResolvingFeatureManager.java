package org.togglz.core.manager;

import java.util.List;
import java.util.Set;

import org.togglz.core.Feature;
import org.togglz.core.activation.ActivationStrategyProvider;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;

/**
 * 
 * A feature manager that delegates all calls to the manager obtained lazily via {@link FeatureContext#getFeatureManager()}.
 * 
 * @author Christian Kaltepoth
 * 
 */
public class LazyResolvingFeatureManager implements FeatureManager {

    private FeatureManager getDelegate() {
        return FeatureContext.getFeatureManager();
    }

    @Override
    public String getName() {
        return getDelegate().getName();
    }

    @Override
    public Set<Feature> getFeatures() {
        return getDelegate().getFeatures();
    }

    @Override
    public FeatureMetaData getMetaData(Feature feature) {
        return getDelegate().getMetaData(feature);
    }

    @Override
    public boolean isActive(Feature feature) {
        return getDelegate().isActive(feature);
    }

    @Override
    public FeatureUser getCurrentFeatureUser() {
        return getDelegate().getCurrentFeatureUser();
    }

    @Override
    public FeatureState getFeatureState(Feature feature) {
        return getDelegate().getFeatureState(feature);
    }

    @Override
    public void setFeatureState(FeatureState state) {
        getDelegate().setFeatureState(state);
    }

    @Override
    public List<ActivationStrategy> getActivationStrategies() {
        return getDelegate().getActivationStrategies();
    }


}
