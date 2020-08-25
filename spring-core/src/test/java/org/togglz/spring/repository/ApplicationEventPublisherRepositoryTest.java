/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.togglz.spring.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 
 * Unit test for {@link ApplicationEventPublisherRepository}.
 * 
 * @author Igor Khudoshin
 * 
 */
class ApplicationEventPublisherRepositoryTest {

    @Mock
    private ApplicationEventPublisher mockApplicationEventPublisher;

    @Mock
    private ApplicationEvent mockEvent;

    private StateRepository delegate;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);

        doNothing().when(mockApplicationEventPublisher).publishEvent(mockEvent);

        delegate = mock(StateRepository.class);
        // the mock supports the ENUM
        when(delegate.getFeatureState(DummyFeature.TEST))
            .thenReturn(new FeatureState(DummyFeature.TEST, true));
    }

    @AfterEach
    public void tearDown() {
        delegate = null;
    }

    @Test
    void saveFeatureStatePublishesEvent() {

        StateRepository repository = new ApplicationEventPublisherRepository(delegate, mockApplicationEventPublisher);

        FeatureState newFeatureState = new FeatureState(DummyFeature.TEST, false);
        repository.setFeatureState(newFeatureState);

        // delegate only called once
        verify(delegate).getFeatureState(DummyFeature.TEST);
        verify(delegate).setFeatureState(newFeatureState);
        verify(mockApplicationEventPublisher).publishEvent(any(FeatureStateChangedEvent.class));
        verifyNoMoreInteractions(delegate);

    }

    private enum DummyFeature implements Feature {
        TEST
    }
}
