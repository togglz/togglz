package org.togglz.core.logging;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

@ExtendWith(MockitoExtension.class)
class LoggingStateRepositoryTest {

    @Mock
    private Logger log;

    @Mock
    private StateRepository delegate;

    @Test
    void shouldDelegateGetFeatureState() {
        // GIVEN
        LoggingStateRepository loggingStateRepository = new LoggingStateRepository(delegate);
        when(delegate.getFeatureState(DummyFeature.TEST)).thenReturn(new FeatureState(DummyFeature.TEST, true));

        // WHEN
        FeatureState featureState = loggingStateRepository.getFeatureState(DummyFeature.TEST);

        // THEN
        assertEquals(DummyFeature.TEST.name(), featureState.getFeature().name());
    }

    @Test
    void shouldDelegateSetFeatureStateAndLog() {
        // GIVEN
        LoggingStateRepository loggingStateRepository = new LoggingStateRepository(delegate, log);

        // WHEN
        loggingStateRepository.setFeatureState(new FeatureState(DummyFeature.TEST, false));

        // THEN
        verify(log).info("Setting Feature \"TEST\" to \"disabled\"");
    }

    @Test
    void shouldDelegateSetFeatureStateAndLogCustomLogMessage() {
        // GIVEN
        LoggingStateRepository loggingStateRepository = new LoggingStateRepository(delegate, "Feature \"{1}\": \"{2}\"", log);

        // WHEN
        loggingStateRepository.setFeatureState(new FeatureState(DummyFeature.TEST, false));

        // THEN
        verify(log).info("Feature \"TEST\": \"disabled\"");
    }

    @Test
    void shouldDelegateSetFeatureStateAndLogCustomLogMessageWithoutPlaceholders() {
        // GIVEN
        LoggingStateRepository loggingStateRepository = new LoggingStateRepository(delegate, "Feature toggled", log);
        new LoggingStateRepository(delegate, "Feature toggled");

        // WHEN
        loggingStateRepository.setFeatureState(new FeatureState(DummyFeature.TEST, false));

        // THEN
        verify(log).info("Feature toggled");
    }

    private enum DummyFeature implements Feature {
        TEST;
    }

}
