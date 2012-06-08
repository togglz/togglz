package org.togglz.core.manager;

import org.togglz.core.Feature;
import org.togglz.core.FeatureMetaData;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.UserProvider;

/**
 * Default implementation of {@link FeatureManager}
 * 
 * @author Christian Kaltepoth
 * 
 */
public class DefaultFeatureManager implements FeatureManager {

    private final StateRepository stateRepository;
    private final Class<? extends Feature> featureClazz;
    private final UserProvider userProvider;

    DefaultFeatureManager(Class<? extends Feature> featureClazz, StateRepository stateRepository,
            UserProvider userProvider) {
        this.featureClazz = featureClazz;
        this.stateRepository = stateRepository;
        this.userProvider = userProvider;
    }

    public Feature[] getFeatures() {
        return featureClazz.getEnumConstants();
    }

    public boolean isActive(Feature feature) {

        FeatureState state = stateRepository.getFeatureState(feature);

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
        FeatureUser user = userProvider.getCurrentUser();
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
        FeatureState state = stateRepository.getFeatureState(feature);
        if (state == null) {
            FeatureMetaData metaData = new FeatureMetaData(feature);
            state = new FeatureState(feature, metaData.isEnabledByDefault());
        }
        return state;
    }

    public void setFeatureState(FeatureState state) {
        stateRepository.setFeatureState(state);
    }

    @Override
    public FeatureUser getCurrentFeatureUser() {
        return userProvider.getCurrentUser();
    }

}
