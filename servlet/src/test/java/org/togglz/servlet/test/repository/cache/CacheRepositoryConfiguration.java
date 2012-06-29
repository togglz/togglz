package org.togglz.servlet.test.repository.cache;

import org.togglz.core.Feature;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.cache.CachingStateRepository;
import org.togglz.core.user.NoOpUserProvider;
import org.togglz.core.user.UserProvider;

public class CacheRepositoryConfiguration implements TogglzConfig {

    @Override
    public Class<? extends Feature> getFeatureClass() {
        return CacheFeatures.class;
    }

    @Override
    public StateRepository getStateRepository() {
        return new CachingStateRepository(new SlowStateRepository(), 10000);
    }

    @Override
    public UserProvider getUserProvider() {
        return new NoOpUserProvider();
    }

}
