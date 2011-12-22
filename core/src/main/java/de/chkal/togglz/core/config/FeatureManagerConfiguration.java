package de.chkal.togglz.core.config;

import de.chkal.togglz.core.Feature;
import de.chkal.togglz.core.repository.FeatureStateRepository;

public interface FeatureManagerConfiguration {

    Feature[] getFeatures();
    
    FeatureStateRepository getFeatureStateRepository();
    
}
