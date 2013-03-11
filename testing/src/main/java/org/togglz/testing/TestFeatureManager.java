package org.togglz.testing;

import java.util.Arrays;
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
    }

    @Override
    public Set<Feature> getFeatures() {
        return new HashSet<Feature>(Arrays.asList(featureClass.getEnumConstants()));
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

    public TestFeatureManager enable(Feature feature) {
        activeFeatures.add(feature);
        return this;
    }

    public TestFeatureManager disable(Feature feature) {
        activeFeatures.remove(feature);
        return this;
    }

    public TestFeatureManager enableAll() {
        for (Feature feature : featureClass.getEnumConstants()) {
            enable(feature);
        }
        return this;
    }

    public TestFeatureManager disableAll() {
        activeFeatures.clear();
        return this;
    }

}
