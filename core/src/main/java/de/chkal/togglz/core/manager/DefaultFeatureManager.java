package de.chkal.togglz.core.manager;

import de.chkal.togglz.core.Feature;
import de.chkal.togglz.core.FeatureMetaData;
import de.chkal.togglz.core.config.FeatureManagerConfiguration;
import de.chkal.togglz.core.repository.FeatureStateRepository;
import de.chkal.togglz.core.user.FeatureUser;
import de.chkal.togglz.core.user.provider.FeatureUserProvider;

public class DefaultFeatureManager implements FeatureManager {

    private final FeatureStateRepository featureStore;
    private final Feature[] features;
    private final FeatureUserProvider featureUserProvider;

    public DefaultFeatureManager(FeatureManagerConfiguration config) {
        this.features = config.getFeatureClass().getEnumConstants();
        this.featureStore = config.getFeatureStateRepository();
        this.featureUserProvider = config.getFeatureUserProvider();
    }

    public Feature[] getFeatures() {
        return features;
    }

    public boolean isActive(Feature feature) {

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
        FeatureUser user = featureUserProvider.getCurrentUser();
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
        FeatureState state = featureStore.getFeatureState(feature);
        if (state == null) {
            FeatureMetaData metaData = new FeatureMetaData(feature);
            state = new FeatureState(feature, metaData.isEnabledByDefault());
        }
        return state;
    }

    public void setFeatureState(FeatureState state) {
        featureStore.setFeatureState(state);
    }

}
