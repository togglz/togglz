package org.togglz.core.logging;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

@RunWith(MockitoJUnitRunner.class)
public class LoggingStateRepositoryTest {

    @Mock
    private Log log;

    @Mock
    private StateRepository delegate;

    @Test
    public void shouldDelegateGetFeatureState() throws Exception {
        // GIVEN
        LoggingStateRepository loggingStateRepository = new LoggingStateRepository(delegate);
        when(delegate.getFeatureState(DummyFeature.TEST)).thenReturn(new FeatureState(DummyFeature.TEST, true));

        // WHEN
        FeatureState featureState = loggingStateRepository.getFeatureState(DummyFeature.TEST);

        // THEN
        assertThat(featureState.getFeature().name(), equalTo(DummyFeature.TEST.name()));
    }

    @Test
    public void shouldDelegateSetFeatureStateAndLog() throws Exception {
        // GIVEN
        LoggingStateRepository loggingStateRepository = new LoggingStateRepository(delegate, log);

        // WHEN
        loggingStateRepository.setFeatureState(new FeatureState(DummyFeature.TEST, false));

        // THEN
        verify(log).info("Setting Feature \"TEST\" to \"disabled\"");
    }

    private enum DummyFeature implements Feature {
        TEST;
    }

}
