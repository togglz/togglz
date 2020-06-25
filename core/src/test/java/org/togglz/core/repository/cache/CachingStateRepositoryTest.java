package org.togglz.core.repository.cache;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.util.NamedFeature;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 
 * Unit test for {@link CachingStateRepository}.
 * 
 * @author Christian Kaltepoth
 * 
 */
class CachingStateRepositoryTest {

    private StateRepository delegate;

    @BeforeEach
    void setUp() {
        delegate = Mockito.mock(StateRepository.class);
        // the mock supports the ENUM
        Mockito.when(delegate.getFeatureState(DummyFeature.TEST))
            .thenReturn(new FeatureState(DummyFeature.TEST, true));
        // and NamedFeature
        Mockito.when(delegate.getFeatureState(new NamedFeature("TEST")))
            .thenReturn(new FeatureState(DummyFeature.TEST, true));
    }

    void tearDown() {
        delegate = null;
    }

    @Test
    void testCachingOfReadOperationsWithTimeToLife() throws InterruptedException {
        StateRepository repository = new CachingStateRepository(delegate, 10000);

        // do some lookups
        for (int i = 0; i < 10; i++) {
            assertTrue(repository.getFeatureState(DummyFeature.TEST).isEnabled());
            Thread.sleep(10);
        }

        // delegate only called once
        Mockito.verify(delegate).getFeatureState(DummyFeature.TEST);
        Mockito.verifyNoMoreInteractions(delegate);
    }

    @Test
    void testCacheWithDifferentFeatureImplementations() throws InterruptedException {

        StateRepository repository = new CachingStateRepository(delegate, 0);

        // do some lookups
        for (int i = 0; i < 10; i++) {
            Feature feature = (i % 2 == 0) ? DummyFeature.TEST :
                new NamedFeature(DummyFeature.TEST.name());
            assertTrue(repository.getFeatureState(feature).isEnabled());
            Thread.sleep(10);
        }

        // delegate only called once
        Mockito.verify(delegate).getFeatureState(DummyFeature.TEST);
        Mockito.verifyNoMoreInteractions(delegate);
    }

    @Test
    void testCachingOfReadOperationsWithoutTimeToLife() throws InterruptedException {

        StateRepository repository = new CachingStateRepository(delegate, 0);

        // do some lookups
        for (int i = 0; i < 10; i++) {
            assertTrue(repository.getFeatureState(DummyFeature.TEST).isEnabled());
            Thread.sleep(10);
        }

        // delegate only called once
        Mockito.verify(delegate).getFeatureState(DummyFeature.TEST);
        Mockito.verifyNoMoreInteractions(delegate);
    }

    @Test
    void testCacheExpiryBecauseOfTimeToLife() throws InterruptedException {

        long ttl = 5;
        StateRepository repository = new CachingStateRepository(delegate, ttl);

        // do some lookups
        for (int i = 0; i < 5; i++) {
            assertTrue(repository.getFeatureState(DummyFeature.TEST).isEnabled());
            Thread.sleep(ttl + 30); // wait some small amount of time to let the cache expire
        }

        // delegate called 5 times
        Mockito.verify(delegate, Mockito.times(5)).getFeatureState(DummyFeature.TEST);
        Mockito.verifyNoMoreInteractions(delegate);
    }

    @Test
    void testStateModifyExpiresCache() throws InterruptedException {

        StateRepository repository = new CachingStateRepository(delegate, 10000);

        // do some lookups
        for (int i = 0; i < 5; i++) {
            assertTrue(repository.getFeatureState(DummyFeature.TEST).isEnabled());
            Thread.sleep(10);
        }

        // now modify the feature state
        repository.setFeatureState(new FeatureState(DummyFeature.TEST, true));

        // do some lookups
        for (int i = 0; i < 5; i++) {
            assertTrue(repository.getFeatureState(DummyFeature.TEST).isEnabled());
            Thread.sleep(10);
        }

        // Check for the correct number of invocations
        Mockito.verify(delegate, Mockito.times(2)).getFeatureState(DummyFeature.TEST);
        Mockito.verify(delegate).setFeatureState(Mockito.any(FeatureState.class));
        Mockito.verifyNoMoreInteractions(delegate);
    }

    @Test
    void shouldFailForNegativeTtl() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new CachingStateRepository(delegate, -1);
        });
    }

    private enum DummyFeature implements Feature {
        TEST
    }
}
