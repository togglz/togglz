package de.chkal.togglz.test.basic.servlet;

import de.chkal.togglz.core.Feature;
import de.chkal.togglz.core.config.FeatureManagerConfiguration;
import de.chkal.togglz.core.manager.FeatureState;
import de.chkal.togglz.core.repository.FeatureStateRepository;
import de.chkal.togglz.core.repository.mem.InMemoryRepository;
import de.chkal.togglz.test.basic.BasicFeatures;

public class ServletFeatureConfiguration implements FeatureManagerConfiguration {

    @Override
    public Feature[] getFeatures() {
        return BasicFeatures.values();
    }

    @Override
    public FeatureStateRepository getFeatureStateRepository() {
        InMemoryRepository repository = new InMemoryRepository();
        repository.setFeatureState(new FeatureState(BasicFeatures.FEATURE2, true));
        return repository;
    }

}
