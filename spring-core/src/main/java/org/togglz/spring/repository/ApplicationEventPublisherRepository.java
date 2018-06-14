
package org.togglz.spring.repository;

import org.springframework.context.ApplicationEventPublisher;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

/**
 * Implementation of {@link StateRepository} that publishes a {@link FeatureStateChangedEvent} when the
 * {@link StateRepository#setFeatureState(FeatureState)} method is called
 * 
 * @author Igor Khudoshin
 */
public class ApplicationEventPublisherRepository implements StateRepository {

    private final StateRepository delegate;

    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * Creates a facade for the supplied {@link StateRepository}.
     * 
     * @param delegate The repository to delegate invocations to
     * @param applicationEventPublisher The {@link ApplicationEventPublisher} to publish the {@link FeatureStateChangedEvent} to
     */
    public ApplicationEventPublisherRepository(StateRepository delegate, ApplicationEventPublisher applicationEventPublisher) {
        this.delegate = delegate;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public FeatureState getFeatureState(Feature feature) {
        return delegate.getFeatureState(feature);
    }

    @Override
    public void setFeatureState(FeatureState featureState) {
        FeatureState previousFeatureState = getFeatureState(featureState.getFeature());
        delegate.setFeatureState(featureState);
        applicationEventPublisher.publishEvent(new FeatureStateChangedEvent(previousFeatureState, featureState));
    }
}
