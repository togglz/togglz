package org.togglz.core.repository.listener;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class ListenableStateRepositoryTest {

    private StateRepository delegate;

    @Before
    public void setup() {
        delegate = new InMemoryStateRepository();
    }

    @Test
    public void shouldReturnNullForUnknownFeature() {
        final ListenableStateRepository repo = new ListenableStateRepository(delegate);
        assertNull(repo.getFeatureState(TestFeature.F1));
    }

    @Test
    public void shouldGetFeatureFromDelegate() {
        final ListenableStateRepository repo = new ListenableStateRepository(delegate);
        delegate.setFeatureState(new FeatureState(TestFeature.F1, true));
        
        assertTrue(repo.getFeatureState(TestFeature.F1).isEnabled());
    }

    @Test
    public void shouldSetFeatureInDelegate() {
        final ListenableStateRepository repo = new ListenableStateRepository(delegate);
        repo.setFeatureState(new FeatureState(TestFeature.F1, true));

        assertTrue(delegate.getFeatureState(TestFeature.F1).isEnabled());
    }

    @Test
    public void shouldNotifyListenersAfterSet() {
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
    public void shouldNotifyListenersInWeightedOrder() {
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
