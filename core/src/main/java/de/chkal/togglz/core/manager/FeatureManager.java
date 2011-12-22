package de.chkal.togglz.core.manager;

import de.chkal.togglz.core.Feature;
import de.chkal.togglz.core.user.FeatureUser;

public interface FeatureManager {

    boolean isActive(Feature feature, FeatureUser user);

    Feature[] getFeatures();

    void setFeatureState(FeatureState state);

    FeatureState getFeatureState(Feature feature);

}
