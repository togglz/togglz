package org.togglz.appengine.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests for MemcacheStateRepository
 * 
 * @author Fábio Franco Uechi
 */
public class MemcacheStateRepositoryTest {

    private final LocalServiceTestHelper helper =
        new LocalServiceTestHelper(new LocalMemcacheServiceTestConfig());

    private MemcacheService ms = MemcacheServiceFactory.getMemcacheService();
    private StateRepository delegate;

    @BeforeEach
    public void setUp() {
        helper.setUp();
        delegate = Mockito.mock(StateRepository.class);
        Mockito.when(delegate.getFeatureState(TestFeature.F1))
            .thenReturn(new FeatureState(TestFeature.F1, true));
    }

    @AfterEach
    public void tearDown() {
        ms.clearAll();
        helper.tearDown();
        delegate = null;
    }

    @Test
    void testCachingOfReadOperationsWithTimeToLife() throws InterruptedException {

        MemcacheStateRepository repository = new MemcacheStateRepository(delegate);

        // do some lookups
        for (int i = 0; i < 10; i++) {
            assertTrue(repository.getFeatureState(TestFeature.F1).isEnabled());
            Thread.sleep(10);
        }

        // delegate only called once
        Mockito.verify(delegate).getFeatureState(TestFeature.F1);
        Mockito.verifyNoMoreInteractions(delegate);

        assertTrue(ms.contains(repository.key(TestFeature.F1.name())));

    }

    @Test
    void testStateModifyExpiresCache() throws InterruptedException {

        MemcacheStateRepository repository = new MemcacheStateRepository(delegate);

        // do some lookups
        for (int i = 0; i < 5; i++) {
            assertTrue(repository.getFeatureState(TestFeature.F1).isEnabled());
            Thread.sleep(10);
        }
        assertTrue(ms.contains(repository.key(TestFeature.F1.name())));

        // now modify the feature state
        repository.setFeatureState(new FeatureState(TestFeature.F1, true));
        assertFalse(ms.contains(repository.key(TestFeature.F1.name())));

        // do some lookups
        for (int i = 0; i < 5; i++) {
            assertTrue(repository.getFeatureState(TestFeature.F1).isEnabled());
            Thread.sleep(10);
        }

        assertTrue(ms.contains(repository.key(TestFeature.F1.name())));
        // Check for the correct number of invocations
        Mockito.verify(delegate, Mockito.times(2)).getFeatureState(TestFeature.F1);
        Mockito.verify(delegate).setFeatureState(Mockito.any(FeatureState.class));
        Mockito.verifyNoMoreInteractions(delegate);

    }

    @Test
    public void testCacheExpiryBecauseOfTimeToLife() throws InterruptedException {

        int ttl = 5;
        MemcacheStateRepository repository = new MemcacheStateRepository(delegate, ttl);

        // do some lookups
        for (int i = 0; i < 5; i++) {
            assertTrue(repository.getFeatureState(TestFeature.F1).isEnabled());
            Thread.sleep(ttl + 10); // wait some minimal amount of time to let the cache expire
        }

        // delegate called 5 times
        Mockito.verify(delegate, Mockito.times(5)).getFeatureState(TestFeature.F1);
        Mockito.verifyNoMoreInteractions(delegate);
    }

    @Test
    public void testNullCaching() throws InterruptedException {
        MemcacheStateRepository repository = new MemcacheStateRepository(delegate);

        // do some lookups
        for (int i = 0; i < 10; i++) {
            assertNull(repository.getFeatureState(TestFeature.F2));
            Thread.sleep(10);
        }

        // delegate only called once
        Mockito.verify(delegate).getFeatureState(TestFeature.F2);
        Mockito.verifyNoMoreInteractions(delegate);
        assertTrue(ms.contains(repository.key(TestFeature.F2.name())));
    }

    private enum TestFeature implements Feature {
        F1, F2
    }

}
