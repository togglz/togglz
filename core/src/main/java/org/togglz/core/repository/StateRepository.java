package org.togglz.core.repository;

import org.togglz.core.Feature;

/**
 * 
 * This interface defines the contract for a class that stores the feature state. Typical implementations are storing the state
 * in a persistent way that works even across application restarts.
 * 
 * @author Christian Kaltepoth
 * 
 */
public interface StateRepository {

    /**
     * Get the persisted state of a feature from the repository. If the repository doesn't contain any information regarding
     * this feature it must return <code>null</code>.
     * 
     * @param feature The feature to read the state for
     * @return The persisted feature state or <code>null</code>
     */
    FeatureState getFeatureState(Feature feature);

    /**
     * Persist the supplied feature state. The repository implementation must ensure that subsequent calls to
     * {@link #getFeatureState(Feature)} return the same state as persisted using this method.
     * 
     * @param featureState The feature state to persist
     * @throws UnsupportedOperationException if this state repository does not support updates
     */
    void setFeatureState(FeatureState featureState);

}
