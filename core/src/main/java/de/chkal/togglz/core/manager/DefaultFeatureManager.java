package de.chkal.togglz.core.manager;

import de.chkal.togglz.core.Feature;
import de.chkal.togglz.core.FeatureMetaData;
import de.chkal.togglz.core.config.FeatureManagerConfiguration;
import de.chkal.togglz.core.repository.FeatureStateRepository;
import de.chkal.togglz.core.user.FeatureUser;

public class DefaultFeatureManager implements FeatureManager {

    private final FeatureStateRepository featureStore;
    private final Feature[] features;

    public DefaultFeatureManager(FeatureManagerConfiguration config) {
        this.features = config.getFeatureClass().getEnumConstants();
        this.featureStore = config.getFeatureStateRepository();
    }

    public Feature[] getFeatures() {
        return features;
    }

    public boolean isActive(Feature feature, FeatureUser user) {

        FeatureState state = featureStore.getFeatureState(feature);
        
        if(state == null) {
            FeatureMetaData metaData = new FeatureMetaData(feature);
            return metaData.isEnabledByDefault();
        }

        // disabled features are never active
        if (!state.isEnabled()) {
            return false;
        }

        // no user restriction? active!
        if (state.getUsers().isEmpty()) {
            return true;
        }

        // check if user is in user list
        if (user != null && user.getName() != null) {
            for (String username : state.getUsers()) {
                if (username.equals(user.getName())) {
                    return true;
                }
            }
        }
        return false;

    }

    public FeatureState getFeatureState(Feature feature) {
        return featureStore.getFeatureState(feature);
    }

    public void setFeatureState(FeatureState state) {
        featureStore.setFeatureState(state);
    }

}
