package org.togglz.core.repository.listener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ListenableStateRepositoryTest {

    private StateRepository delegate;

    @BeforeEach
    public void setup() {
        delegate = new InMemoryStateRepository();
    }

    @Test
    void shouldReturnNullForUnknownFeature() {
        final ListenableStateRepository repo = new ListenableStateRepository(delegate);
        assertNull(repo.getFeatureState(TestFeature.F1));
    }

    @Test
    void shouldGetFeatureFromDelegate() {
        final ListenableStateRepository repo = new ListenableStateRepository(delegate);
        delegate.setFeatureState(new FeatureState(TestFeature.F1, true));
        
        assertTrue(repo.getFeatureState(TestFeature.F1).isEnabled());
    }

    @Test
    void shouldSetFeatureInDelegate() {
        final ListenableStateRepository repo = new ListenableStateRepository(delegate);
        repo.setFeatureState(new FeatureState(TestFeature.F1, true));

        assertTrue(delegate.getFeatureState(TestFeature.F1).isEnabled());
    }

    @Test
    void shouldNotifyListenersAfterSet() {
        FeatureStateChangedListener firstListener = mock(FeatureStateChangedListener.class);
        FeatureStateChangedListener secondListener = mock(FeatureStateChangedListener.class);
        final ListenableStateRepository repo = new ListenableStateRepository(delegate);
        repo.addFeatureStateChangedListener(firstListener);
        repo.addFeatureStateChangedListener(secondListener);

        FeatureState toFeatureState = new FeatureState(TestFeature.F1, true);
        repo.setFeatureState(toFeatureState);

        verify(firstListener, times(1)).onFeatureStateChanged(null, toFeatureState);
        verify(secondListener, times(1)).onFeatureStateChanged(null, toFeatureState);
    }

    @Test
    void shouldNotifyListenersInWeightedOrder() {
        FeatureStateChangedListener higherPrio = mock(FeatureStateChangedListener.class);
        when(higherPrio.priority()).thenReturn(42);
        FeatureStateChangedListener lowerPrio = mock(FeatureStateChangedListener.class);
        when(lowerPrio.priority()).thenReturn(7);
        InOrder inOrder = inOrder(higherPrio, lowerPrio);

        final ListenableStateRepository repo = new ListenableStateRepository(delegate, higherPrio, lowerPrio);
        final FeatureState featureState = new FeatureState(TestFeature.F1, true);
        repo.setFeatureState(featureState);

        inOrder.verify(lowerPrio, times(1)).onFeatureStateChanged(null, featureState);
        inOrder.verify(higherPrio, times(1)).onFeatureStateChanged(null, featureState);
    }

    enum TestFeature implements Feature {
        F1
    }
}
