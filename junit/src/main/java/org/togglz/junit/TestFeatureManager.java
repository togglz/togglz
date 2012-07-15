package org.togglz.junit;

import java.util.HashSet;
import java.util.Set;

import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.FeatureUser;

public class TestFeatureManager implements FeatureManager {

    private final Class<? extends Feature> featureClass;

    private final Set<Feature> activeFeatures = new HashSet<Feature>();

    public TestFeatureManager(Class<? extends Feature> featureClass) {
        this.featureClass = featureClass;
        for (Feature f : getFeatures()) {
            activeFeatures.add(f);
        }
    }

    @Override
    public Feature[] getFeatures() {
        return featureClass.getEnumConstants();
    }

    @Override
    public boolean isActive(Feature feature) {
        return activeFeatures.contains(feature);
    }

    @Override
    public FeatureUser getCurrentFeatureUser() {
        return null;
    }

    @Override
    public FeatureState getFeatureState(Feature feature) {
        return new FeatureState(feature, isActive(feature));
    }

    @Override
    public void setFeatureState(FeatureState state) {
        if (state.isEnabled()) {
            activeFeatures.add(state.getFeature());
        } else {
            activeFeatures.remove(state.getFeature());
        }
    }

    public void enable(Feature feature) {
        activeFeatures.add(feature);
    }

    public void disable(Feature feature) {
        activeFeatures.remove(feature);
    }

}
