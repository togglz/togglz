package org.togglz.core.repository.listener;

import org.togglz.core.repository.FeatureState;
import org.togglz.core.util.Weighted;


/**
 * A listener for feature-state changes.
 */
public interface FeatureStateChangedListener extends Weighted {

    /**
     * Callback method that is called by {@link ListenableStateRepository} instances, when
     * {@link org.togglz.core.repository.StateRepository#setFeatureState(FeatureState)} is
     * called.
     *
     * <p>
     *     The method is invoked after the update in the {@code StateRepository}.
     * </p>
     *
     * @param fromState the previous {@link FeatureState feature state} of the {@link org.togglz.core.Feature feature}
     * @param toState the new value of the FeatureState
     */
    void onFeatureStateChanged(FeatureState fromState, FeatureState toState);

}
