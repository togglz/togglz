package org.togglz.appengine.repository;

import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import java.io.Serializable;

/**
 * Decorates a given StateRepository adding caching capabilities. Leverages GAE's MemcacheServices. Default expiration time is
 * 3600 seconds.
 * 
 * @author Fábio Franco Uechi
 */
public class MemcacheStateRepository implements StateRepository {

    private StateRepository delegate;
    private MemcacheService cache = MemcacheServiceFactory.getMemcacheService();
    private Expiration expiration = Expiration.byDeltaSeconds(3600);
    private static final String KEY_PREFIX = MemcacheStateRepository.class.getName();

    public MemcacheStateRepository(StateRepository delegate) {
        this.delegate = delegate;
    }

    public MemcacheStateRepository(StateRepository delegate, int ttlInSeconds) {
        this(delegate);
        this.expiration = Expiration.byDeltaMillis(ttlInSeconds);
    }

    @Override
    public FeatureState getFeatureState(Feature feature) {
        // first try to find it from the cache
        CacheEntry entry = (CacheEntry) cache.get(key(feature.name()));
        if (entry != null) {
            return entry.getState() != null ? entry.getState() : null;
        }

        // no cache hit
        FeatureState featureState = delegate.getFeatureState(feature);

        // cache the result (may be null)
        cache.put(key(feature.name()), new CacheEntry(featureState != null ? featureState : null), getExpiration());

        // return the result
        return featureState;
    }

    String key(String featureName) {
        return KEY_PREFIX + featureName;
    }

    private Expiration getExpiration() {
        return this.expiration;
    }

    @Override
    public void setFeatureState(FeatureState featureState) {
        delegate.setFeatureState(featureState);
        cache.delete(key(featureState.getFeature().name()));
    }

    /**
     * This class represents a cached repository lookup
     */
    private static class CacheEntry implements Serializable {

        private final FeatureState state;

        public CacheEntry(FeatureState state) {
            this.state = state;
        }

        public FeatureState getState() {
            return state;
        }

    }
}
