package org.togglz.core.manager;

import org.togglz.core.Feature;
import org.togglz.core.FeatureMetaData;
import org.togglz.core.config.TogglzConfig;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.FeatureUserProvider;

/**
 * Default implementation of {@link FeatureManager}
 * 
 * @author Christian Kaltepoth
 * 
 */
public class DefaultFeatureManager implements FeatureManager {

    private final StateRepository featureStore;
    private final Feature[] features;
    private final FeatureUserProvider featureUserProvider;

    public DefaultFeatureManager(TogglzConfig config) {
        this.features = config.getFeatureClass().getEnumConstants();
        this.featureStore = config.getStateRepository();
        this.featureUserProvider = config.getFeatureUserProvider();
    }

    public Feature[] getFeatures() {
        return features;
    }

    public boolean isActive(Feature feature) {

        FeatureState state = featureStore.getFeatureState(feature);

        if (state == null) {
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

    @Override
    public FeatureUser getCurrentFeatureUser() {
        return featureUserProvider.getCurrentUser();
    }

}
