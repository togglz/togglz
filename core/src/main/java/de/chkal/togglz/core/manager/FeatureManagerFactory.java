package de.chkal.togglz.core.manager;

import de.chkal.togglz.core.config.FeatureManagerConfiguration;

public class FeatureManagerFactory {

    public FeatureManager build(FeatureManagerConfiguration config) {
        return new DefaultFeatureManager(config);
    }

}
