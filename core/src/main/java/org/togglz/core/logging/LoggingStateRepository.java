package org.togglz.core.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final Logger log;

    private final StateRepository delegate;

    private String customLogMessage;

    /**
     * Creates a logging facade for the supplied {@link StateRepository}.
     *
     * @param delegate The repository to delegate invocations to
     */
    public LoggingStateRepository(StateRepository delegate) {
        this(delegate, LoggerFactory.getLogger(LoggingStateRepository.class));
    }

    /**
     * Creates a logging facade for the supplied {@link StateRepository} using a custom log message. In your custom log message,
     * mark the position of the feature name as {1} and the position for the new feature state which will be replaced by
     * "enabled" or "disabled" as {2}.
     *
     * @param delegate The repository to delegate invocations to
     * @param customLogMessage Your custom log message. It may contain a placeholder {1} for the feature name and a placeholder
     *        {2} for the new feature state which will be replaced by "enabled" or "disabled"
     */
    public LoggingStateRepository(StateRepository delegate, String customLogMessage) {
        this(delegate, customLogMessage, LoggerFactory.getLogger(LoggingStateRepository.class));
    }

    protected LoggingStateRepository(StateRepository delegate, Logger log) {
        this.delegate = delegate;
        this.log = log;
    }

    protected LoggingStateRepository(StateRepository delegate, String customLogMessage, Logger log) {
        this.delegate = delegate;
        this.log = log;
        this.customLogMessage = customLogMessage;
    }

    @Override
    public FeatureState getFeatureState(Feature feature) {
        return delegate.getFeatureState(feature);
    }

    @Override
    public void setFeatureState(FeatureState featureState) {
        if (customLogMessage != null) {
            log.info(createCustomLogMessage(featureState));
        } else {
            log.info(createDefaultLogMessage(featureState));
        }
        delegate.setFeatureState(featureState);
    }

    private String createDefaultLogMessage(FeatureState featureState) {
        return "Setting Feature \"" + featureState.getFeature().name()
            + "\" to \"" + getReadableFeatureState(featureState) + "\"";
    }

    private String createCustomLogMessage(FeatureState featureState) {
        String logMsg = customLogMessage;
        logMsg = logMsg.replace("{1}", featureState.getFeature().name());
        logMsg = logMsg.replace("{2}", getReadableFeatureState(featureState));
        return logMsg;
    }

    private String getReadableFeatureState(FeatureState featureState) {
        return featureState.isEnabled() ? "enabled" : "disabled";
    }

}
