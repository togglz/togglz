package de.chkal.togglz.test.basic.spring;

import org.springframework.stereotype.Component;

import de.chkal.togglz.core.Feature;
import de.chkal.togglz.core.config.FeatureManagerConfiguration;
import de.chkal.togglz.core.repository.FeatureStateRepository;
import de.chkal.togglz.core.repository.mem.InMemoryRepository;
import de.chkal.togglz.test.basic.BasicFeatures;

@Component
public class SpringFeatureConfiguration implements FeatureManagerConfiguration {

    @Override
    public Class<? extends Feature> getFeatureClass() {
        return BasicFeatures.class;
    }

    @Override
    public FeatureStateRepository getFeatureStateRepository() {
        return new InMemoryRepository();
    }

}
