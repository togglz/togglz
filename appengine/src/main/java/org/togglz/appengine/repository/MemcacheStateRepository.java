package org.togglz.appengine.repository;

import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

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

    public MemcacheStateRepository(StateRepository delegate) {
        this.delegate = delegate;
    }

    public MemcacheStateRepository(StateRepository delegate, Integer ttlInSeconds) {
        this(delegate);
        this.expiration = Expiration.byDeltaMillis(ttlInSeconds);
    }

    @Override
    public FeatureState getFeatureState(Feature feature) {
        FeatureState state = (FeatureState) cache.get(feature.name());
        if (state == null) {
            state = delegate.getFeatureState(feature);
            cache.put(feature.name(), state, getExpiration());
        }
        return state;
    }

    private Expiration getExpiration() {
        return this.expiration;
    }

    @Override
    public void setFeatureState(FeatureState featureState) {
        delegate.setFeatureState(featureState);
        cache.delete(featureState.getFeature().name());
    }
}
