package org.togglz.testing.fallback;

import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.FeatureUser;

public class FallbackTestFeatureManager implements FeatureManager {

    @Override
    public Feature[] getFeatures() {
        return new Feature[0];
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

}
