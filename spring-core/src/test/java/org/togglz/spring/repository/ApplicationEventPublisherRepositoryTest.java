
package org.togglz.spring.repository;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

/**
 * 
 * Unit test for {@link ApplicationEventPublisherRepository}.
 * 
 * @author Igor Khudoshin
 * 
 */
public class ApplicationEventPublisherRepositoryTest {
    @Mock
    private ApplicationEventPublisher mockApplicationEventPublisher;
    @Mock
    private ApplicationEvent mockEvent;

    private StateRepository delegate;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        doNothing().when(mockApplicationEventPublisher).publishEvent(mockEvent);

        delegate = Mockito.mock(StateRepository.class);
        // the mock supports the ENUM
        Mockito.when(delegate.getFeatureState(DummyFeature.TEST))
            .thenReturn(new FeatureState(DummyFeature.TEST, true));
    }

    public void tearDown() {
        delegate = null;
    }

    @Test
    public void testSaveFeatureStatePublishesEvent() {

        StateRepository repository = new ApplicationEventPublisherRepository(delegate, mockApplicationEventPublisher);

        FeatureState newFeatureState = new FeatureState(DummyFeature.TEST, false);
        repository.setFeatureState(newFeatureState);

        // delegate only called once
        Mockito.verify(delegate).getFeatureState(DummyFeature.TEST);
        Mockito.verify(delegate).setFeatureState(newFeatureState);
        Mockito.verify(mockApplicationEventPublisher).publishEvent(any(FeatureStateChangedEvent.class));
        Mockito.verifyNoMoreInteractions(delegate);

    }

    private enum DummyFeature implements Feature {
        TEST;
    }

}
