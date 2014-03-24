package org.togglz.core.spi;

import org.togglz.core.metadata.FeatureRuntimeAttributes;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.FeatureUser;

/**
 * This interface represents a custom strategy for deciding whether a feature is active or not. It extends the basic strategy to
 * allow to decide if the Feature is enabled depending on runtime attributes not tied to the current user.
 * 
 * @author Fabien Chaillou
 */
public interface ActivationStrategyWithRuntimeAttributes extends ActivationStrategy {

    /**
     * This method is responsible to decide whether a feature is active or not. The implementation can use the custom
     * configuration parameters of the strategy stored in the feature state, information from the currently acting user and
     * informations from the runtimesAttributes to find a decision.
     * 
     * @param featureState The feature state which represents the current configuration of the feature. The implementation of
     *        the method typically uses {@link org.togglz.core.repository.FeatureState#getParameter(String)} to access custom
     *        configuration paramater values.
     * @param user The user for which to decide whether the feature is active. May be <code>null</code> if the user could not be
     *        identified by the {@link org.togglz.core.user.UserProvider}.
     * @param runtimeAttributes Runtime attributes not associated to the user for which to decide if the feature is active.
     * 
     * 
     * @return <code>true</code> if the feature should be active, else <code>false</code>
     */
    boolean isActive(FeatureState featureState, FeatureUser user, FeatureRuntimeAttributes runtimeAttributes);
}
