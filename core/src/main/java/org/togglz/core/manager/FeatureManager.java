package org.togglz.core.manager;

import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.FeatureUserProvider;

/**
 * 
 * The {@link FeatureManager} is the central class in Togglz. It's typically obtained using
 * {@link FeatureContext#getFeatureManager()}.
 * 
 * @author Christian Kaltepoth
 * 
 */
public interface FeatureManager {

    /**
     * Provides a list of all features the manager is responsible for.
     * 
     * @return Array of features, never <code>null</code>
     */
    Feature[] getFeatures();

    /**
     * Checks whether the supplied feature is active or not. Please note that this method will internally use the
     * {@link FeatureUserProvider} to obtain the currently acting user as it may be relevant if the feature is enabled only for
     * specific set of users.
     * 
     * @param feature The feature to check
     * @return <code>true</code> if the feature is active, <code>false</code> otherwise
     */
    boolean isActive(Feature feature);

    /**
     * Get the current feature user. This method will internally use the configured {@link FeatureUserProvider} to obtain the
     * user.
     * 
     * @return The current {@link FeatureUser} or null if the {@link FeatureUserProvider} didn't return any result.
     */
    FeatureUser getCurrentFeatureUser();

    /**
     * Returns the {@link FeatureState} for the specified feature. This state represents the current configuration of the
     * feature and is typically persisted by a {@link StateRepository} across JVM restarts. The state includes whether
     * the feature is enabled or disabled and the use list.
     * 
     * @param feature The feature to get the state for
     * @return The current state of the feature, never <code>null</code>.
     */
    FeatureState getFeatureState(Feature feature);

    /**
     * Updates the state of a feature. THis allows to enable or disable a feature and to modify the user list associated with
     * the feature.
     * 
     * @param state The new feature state.
     */
    void setFeatureState(FeatureState state);

}
