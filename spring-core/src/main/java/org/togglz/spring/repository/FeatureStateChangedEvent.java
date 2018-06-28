
package org.togglz.spring.repository;

import org.springframework.context.ApplicationEvent;
import org.togglz.core.repository.FeatureState;

/**
 * An {@link ApplicationEvent} that is published whenever a {@link FeatureState} is changed
 *
 * @author Igor Khudoshin
 */
public class FeatureStateChangedEvent extends ApplicationEvent {
    private static final long serialVersionUID = 9114298135037191136L;

    private FeatureState previousFeatureState;

    public FeatureState getPreviousFeatureState() {
        return previousFeatureState;
    }

    public FeatureState getFeatureState() {
        return (FeatureState) getSource();
    }

    public FeatureStateChangedEvent(FeatureState previousFeatureState, FeatureState featureState) {
        super(FeatureState.copyOf(featureState));
        this.previousFeatureState = FeatureState.copyOf(previousFeatureState);
    }
}
