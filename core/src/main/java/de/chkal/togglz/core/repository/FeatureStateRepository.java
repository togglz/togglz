package de.chkal.togglz.core.repository;

import de.chkal.togglz.core.Feature;
import de.chkal.togglz.core.manager.FeatureState;

public interface FeatureStateRepository {

    FeatureState getFeatureState(Feature feature);

    void setFeatureState(FeatureState featureState);

}
