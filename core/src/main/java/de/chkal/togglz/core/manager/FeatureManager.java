package de.chkal.togglz.core.manager;

import de.chkal.togglz.core.Feature;

public interface FeatureManager {

    boolean isActive(Feature feature);

    Feature[] getFeatures();

    void setFeatureState(FeatureState state);

    FeatureState getFeatureState(Feature feature);

}
