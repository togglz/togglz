package de.chkal.togglz.core.config;

import de.chkal.togglz.core.Feature;
import de.chkal.togglz.core.repository.FeatureStateRepository;
import de.chkal.togglz.core.user.FeatureUserProvider;

public interface FeatureManagerConfiguration {

    Class<? extends Feature> getFeatureClass();

    FeatureStateRepository getFeatureStateRepository();

    FeatureUserProvider getFeatureUserProvider();

}
