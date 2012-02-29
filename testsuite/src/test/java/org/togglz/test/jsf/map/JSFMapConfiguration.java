package org.togglz.test.jsf.map;

import javax.enterprise.context.ApplicationScoped;

import org.togglz.core.Feature;
import org.togglz.core.config.TogglzConfig;
import org.togglz.core.repository.FeatureStateRepository;
import org.togglz.core.repository.mem.InMemoryRepository;
import org.togglz.core.user.FeatureUserProvider;
import org.togglz.core.user.NoOpFeatureUserProvider;
import org.togglz.test.jsf.JSFFeatures;


@ApplicationScoped
public class JSFMapConfiguration implements TogglzConfig {

    @Override
    public Class<? extends Feature> getFeatureClass() {
        return JSFFeatures.class;
    }

    @Override
    public FeatureStateRepository getFeatureStateRepository() {
        return new InMemoryRepository();
    }

    @Override
    public FeatureUserProvider getFeatureUserProvider() {
        return new NoOpFeatureUserProvider();
    }

}
