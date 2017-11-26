package org.togglz.core.logging;

import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

/**
 * 
 * Simple implementation of {@link StateRepository} which adds logging capabilities to an existing repository. You should
 * consider using this class if you want to log all feature toggled events.
 * 
 * @author Martin Günther
 * 
 */
public class LoggingStateRepository implements StateRepository {

    private final Log log;

    private final StateRepository delegate;

    /**
     * Creates a logging facade for the supplied {@link StateRepository}.
     * 
     * @param delegate The repository to delegate invocations to
     */
    public LoggingStateRepository(StateRepository delegate) {
        this(delegate, LogFactory.getLog(LoggingStateRepository.class));
    }

    protected LoggingStateRepository(StateRepository delegate, Log log) {
        this.delegate = delegate;
        this.log = log;
    }

    @Override
    public FeatureState getFeatureState(Feature feature) {
        return delegate.getFeatureState(feature);
    }

    @Override
    public void setFeatureState(FeatureState featureState) {
        log.info("Setting Feature \"" + featureState.getFeature().name()
            + "\" to \"" + getReadableFeatureState(featureState) + "\"");
        delegate.setFeatureState(featureState);

    }

    private String getReadableFeatureState(FeatureState featureState) {
        return featureState.isEnabled() ? "enabled" : "disabled";
    }

}
