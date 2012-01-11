package de.chkal.togglz.test.jsf.map;

import javax.enterprise.context.ApplicationScoped;

import de.chkal.togglz.core.Feature;
import de.chkal.togglz.core.config.FeatureManagerConfiguration;
import de.chkal.togglz.core.repository.FeatureStateRepository;
import de.chkal.togglz.core.repository.mem.InMemoryRepository;
import de.chkal.togglz.core.user.FeatureUserProvider;
import de.chkal.togglz.core.user.NoOpFeatureUserProvider;
import de.chkal.togglz.test.jsf.JSFFeatures;

@ApplicationScoped
public class JSFMapConfiguration implements FeatureManagerConfiguration {

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
