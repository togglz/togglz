package org.togglz.appengine.repository;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

/**
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
        this.expiration = Expiration.byDeltaSeconds(ttlInSeconds);
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
