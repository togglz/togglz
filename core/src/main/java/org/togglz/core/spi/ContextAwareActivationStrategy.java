package org.togglz.core.spi;

import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.UserProvider;

/**
 * This interface represents a custom context-aware strategy for deciding whether a feature is active or not.
 *
 * @author Philip Sanetra
 */
public interface ContextAwareActivationStrategy<T> extends ActivationStrategy {

    /**
     * @deprecated This method is not supported!
     */
    @Override
    default boolean isActive(FeatureState featureState, FeatureUser user) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is responsible to decide whether a feature is active or not. The implementation can use the custom
     * configuration parameters of the strategy stored in the feature state, information from the currently acting user
     * and information from a context object to find a decision.
     *
     * @param featureState The feature state which represents the current configuration of the feature. The implementation of
     *        the method typically uses {@link FeatureState#getParameter(String)} to access custom configuration paramater
     *        values.
     * @param user The user for which to decide whether the feature is active. May be <code>null</code> if the user could not be
     *        identified by the {@link UserProvider}.
     * @param context The context for which to decide whether the feature is active.
     *
     * @return <code>true</code> if the feature should be active, else <code>false</code>
     */
    boolean isActive(FeatureState featureState, FeatureUser user, T context);

}
