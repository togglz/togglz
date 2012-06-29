package org.togglz.core.repository.cache;

import java.util.HashMap;
import java.util.Map;

import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

/**
 * 
 * Simple implementation of {@link StateRepository} which adds caching capabilities to an existing repository. You should
 * consider using this class if lookups in your {@link StateRepository} are expensive (like database queries).
 * 
 * @author Christian Kaltepoth
 * 
 */
public class CachingStateRepository implements StateRepository {

    private final StateRepository delegate;

    private final Map<Feature, CacheEntry> cache = new HashMap<Feature, CacheEntry>();

    private long ttl;

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
     * @param ttl The time in milliseconds after which a cache entry will expire
     */
    public CachingStateRepository(StateRepository delegate, long ttl) {
        this.delegate = delegate;
        this.ttl = ttl;
    }

    @Override
    public FeatureState getFeatureState(Feature feature) {

        // first try to find it from the cache
        CacheEntry entry = cache.get(feature);
        if (entry != null && !isExpired(entry)) {
            return entry.getState() != null ? entry.getState().copy() : null;
        }

        // no cache hit
        FeatureState featureState = delegate.getFeatureState(feature);

        // cache the result (may be null)
        cache.put(feature, new CacheEntry(featureState != null ? featureState.copy() : null));

        // return the result
        return featureState;

    }

    @Override
    public void setFeatureState(FeatureState featureState) {
        delegate.setFeatureState(featureState);
        cache.remove(featureState.getFeature());
    }

    /**
     * Checks whether this supplied {@link CacheEntry} should be ignored.
     */
    private boolean isExpired(CacheEntry entry) {
        if (ttl > 0) {
            return entry.getTimestamp() + ttl < System.currentTimeMillis();
        }
        return false;
    }

    /**
     * This class represents a cached repository lookup
     */
    private static class CacheEntry {

        private final FeatureState state;

        private final long timestamp;

        public CacheEntry(FeatureState state) {
            this.state = state;
            this.timestamp = System.currentTimeMillis();
        }

        public FeatureState getState() {
            return state;
        }

        public long getTimestamp() {
            return timestamp;
        }

    }

}
