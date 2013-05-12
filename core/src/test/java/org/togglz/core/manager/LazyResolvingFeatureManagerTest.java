package org.togglz.core.manager;

import org.junit.Test;
import org.togglz.core.Feature;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LazyResolvingFeatureManagerTest {

    @Test
    public void canDelegateToInjectedFeatureManager() {
        LazyResolvingFeatureManager manager = new LazyResolvingFeatureManager();
        // Given
        FeatureManager delegate = mock(FeatureManager.class);
        manager.setDelegate(delegate);
        // When
        Feature feature = mock(Feature.class);
        manager.isActive(feature);
        // Then
        verify(delegate).isActive(feature);
    }

}
