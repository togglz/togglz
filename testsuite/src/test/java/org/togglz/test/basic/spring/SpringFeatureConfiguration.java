package org.togglz.test.basic.spring;

import org.springframework.stereotype.Component;
import org.togglz.core.Feature;
import org.togglz.core.config.TogglzConfig;
import org.togglz.core.repository.FeatureStateRepository;
import org.togglz.core.repository.mem.InMemoryRepository;
import org.togglz.core.user.FeatureUserProvider;
import org.togglz.core.user.NoOpFeatureUserProvider;
import org.togglz.test.basic.BasicFeatures;


@Component
public class SpringFeatureConfiguration implements TogglzConfig {

    @Override
    public Class<? extends Feature> getFeatureClass() {
        return BasicFeatures.class;
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
