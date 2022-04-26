package org.togglz.core.repository.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

/**
 * Simple implementation of {@link StateRepository} which adds caching capabilities to an existing repository. You should
 * consider using this class if lookups in your {@link StateRepository} are expensive (like database queries).
 *
 * @author Christian Kaltepoth
 */
public class CachingStateRepository implements StateRepository {

    private final StateRepository delegate;

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    private final long ttl;

    /**
     * Creates a caching facade for the supplied {@link StateRepository}. The cached state of a feature will only expire if
     * {@link #setFeatureState(FeatureState)} is invoked. You should therefore never use this constructor if the feature state
     * is modified directly (for example by modifying the database table or the properties file).
     *
     * @param delegate The repository to delegate invocations to
     */
    public CachingStateRepository(StateRepository delegate) {
        this(delegate, 0);
    }

    /**
     * Creates a caching facade for the supplied {@link StateRepository}. The cached state of a feature will expire after the
     * supplied TTL or if {@link #setFeatureState(FeatureState)} is invoked.
     *
     * @param delegate The repository to delegate invocations to
     * @param ttl      The time in milliseconds after which a cache entry will expire
     * @throws IllegalArgumentException if the specified ttl is negative
     */
    public CachingStateRepository(StateRepository delegate, long ttl) {
        if (ttl < 0) {
            throw new IllegalArgumentException("Negative TTL value: " + ttl);
        }

        this.delegate = delegate;
        this.ttl = ttl;
    }

    /**
     * Creates a caching facade for the supplied {@link StateRepository}. The cached state of a feature will expire after the
     * supplied TTL rounded down to milliseconds or if {@link #setFeatureState(FeatureState)} is invoked.
     *
     * @param delegate    The repository to delegate invocations to
     * @param ttl         The time in a given {@code ttlTimeUnit} after which a cache entry will expire
     * @param ttlTimeUnit The unit that {@code ttl} is expressed in
     */
    public CachingStateRepository(StateRepository delegate, long ttl, TimeUnit ttlTimeUnit) {
        this(delegate, ttlTimeUnit.toMillis(ttl));
    }

    @Override
    public FeatureState getFeatureState(Feature feature) {
        // first try to find it from the cache
        CacheEntry entry = cache.get(feature.name());
        if (isValidEntry(entry)) {
            return entry.getState();
        }
        // no cache hit
        return reloadFeatureState(feature);
    }

    private synchronized FeatureState reloadFeatureState(Feature feature) {
        CacheEntry cachedState = cache.get(feature.name());
        if (isValidEntry(cachedState)) {
            return cachedState.getState();
        }
        FeatureState featureState = delegate.getFeatureState(feature);
        storeFeatureState(feature, featureState);
        return featureState;
    }

    private void storeFeatureState(Feature feature, FeatureState featureState) {
        cache.put(feature.name(), new CacheEntry(featureState != null ? featureState.copy() : null, ttl));
    }

    private boolean isValidEntry(CacheEntry entry) {
        return entry != null && !entry.isExpired();
    }

    @Override
    public void setFeatureState(FeatureState featureState) {
        delegate.setFeatureState(featureState);
        cache.remove(featureState.getFeature().name());
    }

    /**
     * Clears the contents of the cache
     */
    public void clear() {
        cache.clear();
    }

    /**
     * This class represents a cached repository lookup
     */
    private static class CacheEntry {

        private final FeatureState state;

        private final long timestamp;

        private final long ttl;

        public CacheEntry(FeatureState state, final long ttl) {
            this.state = state;
            this.timestamp = System.currentTimeMillis();
            this.ttl = ttl;
        }

        public FeatureState getState() {
            return state;
        }

        public boolean isExpired() {
            if (ttl == 0) {
                return false;
            }
            return timestamp + ttl < System.currentTimeMillis();
        }
    }

}
