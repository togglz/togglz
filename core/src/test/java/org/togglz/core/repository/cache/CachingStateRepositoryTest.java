package org.togglz.core.repository.cache;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.util.NamedFeature;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;

class CachingStateRepositoryTest {

    private final StateRepository delegate = Mockito.spy(new InMemoryStateRepository());

    @BeforeEach
    void setUp() {
        // the mock supports the ENUM
        delegate.setFeatureState(new FeatureState(DummyFeature.TEST, true));
        // and NamedFeature
        delegate.setFeatureState(new FeatureState(new NamedFeature("TEST"), true));
        Mockito.clearInvocations(delegate);
    }

    @Test
    void cachingOfReadOperationsWithTimeToLive() {
        // given
        StateRepository repository = new CachingStateRepository(delegate, 10000);

        // when
        for (int i = 0; i < 10; i++) {
            assertTrue(repository.getFeatureState(DummyFeature.TEST).isEnabled());
            tickClock(Duration.ofMillis(10));
        }
        // then
        Mockito.verify(delegate, times(1)).getFeatureState(DummyFeature.TEST);
    }

    @Test
    void testCacheWithDifferentFeatureImplementations() {

        StateRepository repository = new CachingStateRepository(delegate, 0);

        // do some lookups
        for (int i = 0; i < 10; i++) {
            Feature feature = (i % 2 == 0) ? DummyFeature.TEST :
                new NamedFeature(DummyFeature.TEST.name());
            assertTrue(repository.getFeatureState(feature).isEnabled());
            tickClock(Duration.ofMillis(10));
        }

        // delegate only called once
        Mockito.verify(delegate).getFeatureState(DummyFeature.TEST);
        Mockito.verifyNoMoreInteractions(delegate);
    }

    @Test
    void usesTheCacheForTtlOfZero() {

        StateRepository repository = new CachingStateRepository(delegate, 0);

        // do some lookups
        for (int i = 0; i < 10; i++) {
            assertTrue(repository.getFeatureState(DummyFeature.TEST).isEnabled());
            tickClock(Duration.ofMillis(10));
        }

        // delegate only called once
        Mockito.verify(delegate).getFeatureState(DummyFeature.TEST);
        Mockito.verifyNoMoreInteractions(delegate);
    }

    @Test
    void delegateIsUsedWhenCacheExpires() {
        // given
        long ttl = 5;
        StateRepository repository = new CachingStateRepository(delegate, ttl);

        // when
        for (int i = 0; i < 5; i++) {
            assertTrue(repository.getFeatureState(DummyFeature.TEST).isEnabled());
            tickClock(Duration.ofMillis(ttl + 30)); // wait some small amount of time to let the cache expire
        }

        // then
        Mockito.verify(delegate, times(5)).getFeatureState(DummyFeature.TEST);
        Mockito.verifyNoMoreInteractions(delegate);
    }

    @Test
    void stateModifyExpiresCache() {

        StateRepository repository = new CachingStateRepository(delegate, 10000);

        // do some lookups
        for (int i = 0; i < 5; i++) {
            assertTrue(repository.getFeatureState(DummyFeature.TEST).isEnabled());
            tickClock(Duration.ofMillis(10));
        }

        // now modify the feature state
        repository.setFeatureState(new FeatureState(DummyFeature.TEST, true));

        // do some lookups
        for (int i = 0; i < 5; i++) {
            assertTrue(repository.getFeatureState(DummyFeature.TEST).isEnabled());
            tickClock(Duration.ofMillis(10));
        }

        // Check for the correct number of invocations
        Mockito.verify(delegate, times(2)).getFeatureState(DummyFeature.TEST);
        Mockito.verify(delegate).setFeatureState(Mockito.any(FeatureState.class));
        Mockito.verifyNoMoreInteractions(delegate);
    }

    @Test
    void shouldFailForNegativeTtl() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new CachingStateRepository(delegate, -1));
    }

    @Nested
    class AsyncCacheStateRepositoryTest {

        public static class SlowStateRepository implements StateRepository {

            private final InMemoryStateRepository delegate = new InMemoryStateRepository();
            private Duration delay = Duration.ofSeconds(2);
            private CountDownLatch latch;

            @Override
            public FeatureState getFeatureState(Feature feature) {
                Instant start = CachingStateRepository.clock.instant();
                Instant finish = start.plus(delay);
                if (latch != null) {
                    latch.countDown();
                }
                System.out.println("["+Thread.currentThread().getName()+"] getting... start: " + start + " finish: " + finish + " clock: " + CachingStateRepository.clock.instant());
                while (CachingStateRepository.clock.instant().isBefore(finish)) {
                    // busy wait
                    Thread.onSpinWait();
                }
                System.out.println("["+Thread.currentThread().getName()+"] got start: " + start + " finish: " + finish + " clock: " + CachingStateRepository.clock.instant());
                FeatureState featureState = delegate.getFeatureState(feature);
                System.out.println("["+Thread.currentThread().getName()+"] returning feature: " + featureState.getFeature().name() + " enabled: " + featureState.isEnabled());
                return featureState;
            }

            @Override
            public void setFeatureState(FeatureState featureState) {
                delegate.setFeatureState(featureState);
            }

        }
        private final SlowStateRepository slowRepository = Mockito.spy(new SlowStateRepository());

        @BeforeEach
        void setUp() {
            slowRepository.setFeatureState(new FeatureState(DummyFeature.TEST, true));
            Mockito.clearInvocations(slowRepository);
        }

        @AfterEach
        void tearDown() {
            tickClock(slowRepository.delay);
        }

        @Test
        void returnsNullOnFirstCall() throws InterruptedException {
            // given
            slowRepository.latch = new CountDownLatch(1);
            StateRepository repository = new CachingStateRepository(slowRepository, 10000, Executors.newSingleThreadExecutor());

            // when
            FeatureState featureState = repository.getFeatureState(DummyFeature.TEST);
            slowRepository.latch.await();

            // then
            assertNull(featureState);
            Mockito.verify(slowRepository).getFeatureState(DummyFeature.TEST);
            Mockito.verifyNoMoreInteractions(slowRepository);
        }

        @Test
        void returnsValueWhenItIsFetchedAndSlowRepositoryIsCalledOnlyOnce() throws InterruptedException {
            // given
            StateRepository repository = new CachingStateRepository(slowRepository, 10000, Executors.newSingleThreadExecutor());
            Duration halfDelay = slowRepository.delay.dividedBy(2);
            slowRepository.latch = new CountDownLatch(1);

            // when
            assertNull(repository.getFeatureState(DummyFeature.TEST));
            slowRepository.latch.await();
            tickClock(halfDelay);
            assertNull(repository.getFeatureState(DummyFeature.TEST));
            tickClock(halfDelay);

            // then
            Awaitility
                    .await()
                    .atMost(200, MILLISECONDS)
                    .pollDelay(5, MILLISECONDS)
                    .untilAsserted(() -> assertTrue(repository.getFeatureState(DummyFeature.TEST).isEnabled()));
            Mockito.verify(slowRepository, Mockito.only()).getFeatureState(DummyFeature.TEST);
        }

        @Test
        void updatesCacheOnEntryExpirationAsynchronously() throws InterruptedException {
            // given
            Duration ttl = Duration.ofSeconds(10);
            StateRepository repository = new CachingStateRepository(slowRepository,
                                                                    ttl.toMillis(),
                                                                    Executors.newSingleThreadExecutor());
            slowRepository.latch = new CountDownLatch(1);

            // when
            assertNull(repository.getFeatureState(DummyFeature.TEST));
            slowRepository.latch.await();
            tickClock(slowRepository.delay);
            Awaitility
                    .await()
                    .atMost(200, MILLISECONDS)
                    .pollDelay(5, MILLISECONDS)
                    .untilAsserted(() -> {
                        assertNotNull(repository.getFeatureState(DummyFeature.TEST));
                        assertTrue(repository.getFeatureState(DummyFeature.TEST).isEnabled());
                    });

            System.out.println("["+Thread.currentThread().getName()+"] first call");

            // state in repository is changed
            slowRepository.setFeatureState(new FeatureState(DummyFeature.TEST, false));

            // cache entity expires
            tickClock(ttl.plus(Duration.ofSeconds(2)));

            // repository returns old value until cache is updated
            slowRepository.latch = new CountDownLatch(1);
            assertTrue(repository.getFeatureState(DummyFeature.TEST).isEnabled());
            slowRepository.latch.await();
            tickClock(slowRepository.delay);
            Awaitility
                    .await()
                    .atMost(200, MILLISECONDS)
                    .pollDelay(5, MILLISECONDS)
                    .untilAsserted(() -> {
                        assertFalse(repository.getFeatureState(DummyFeature.TEST).isEnabled());
                    });

            // then slow repository is called only twice (initial and after cache expiration)
            Mockito.verify(slowRepository, times(2)).getFeatureState(DummyFeature.TEST);
            Mockito.verify(slowRepository).setFeatureState(Mockito.any());
            Mockito.verifyNoMoreInteractions(slowRepository);
        }

        @Test
        void fetchingTwoFeatureFlagsDoNotBlockEachOther() throws InterruptedException {
            // given
            Duration ttl = Duration.ofSeconds(10);
            slowRepository.latch = new CountDownLatch(2);
            slowRepository.setFeatureState(new FeatureState(DummyFeature.TEST2, false));
            Mockito.clearInvocations(slowRepository);

            StateRepository repository = new CachingStateRepository(slowRepository,
                                                                    ttl.toMillis(),
                                                                    Executors.newFixedThreadPool(2));
            assertNull(repository.getFeatureState(DummyFeature.TEST));
            assertNull(repository.getFeatureState(DummyFeature.TEST2));

            // both requests are in progress slowly downloaded from the slow repository
            slowRepository.latch.await();


            // when the time passes
            tickClock(slowRepository.delay);

            // then both features appears under the twice of slow repository delay
            Awaitility
                    .await()
                    .atMost(200, MILLISECONDS)
                    .pollDelay(5, MILLISECONDS)
                    .untilAsserted(() -> {
                        assertNotNull(repository.getFeatureState(DummyFeature.TEST));
                        assertNotNull(repository.getFeatureState(DummyFeature.TEST2));
                        assertTrue(repository.getFeatureState(DummyFeature.TEST).isEnabled());
                        assertFalse(repository.getFeatureState(DummyFeature.TEST2).isEnabled());
                    });
        }

    }

    private enum DummyFeature implements Feature {
        TEST,
        TEST2
    }

    private static void tickClock(Duration tickDuration) {
        CachingStateRepository.clock = Clock.offset(CachingStateRepository.clock, tickDuration);
    }
}
