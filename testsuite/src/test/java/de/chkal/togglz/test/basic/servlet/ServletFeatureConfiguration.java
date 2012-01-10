package de.chkal.togglz.test.basic.servlet;

import de.chkal.togglz.core.Feature;
import de.chkal.togglz.core.config.FeatureManagerConfiguration;
import de.chkal.togglz.core.repository.FeatureStateRepository;
import de.chkal.togglz.core.repository.mem.InMemoryRepository;
import de.chkal.togglz.core.user.FeatureUserProvider;
import de.chkal.togglz.core.user.NoOpFeatureUserProvider;
import de.chkal.togglz.test.basic.BasicFeatures;

public class ServletFeatureConfiguration implements FeatureManagerConfiguration {

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
