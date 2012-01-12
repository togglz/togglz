package de.chkal.togglz.cdi;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import de.chkal.togglz.core.context.FeatureContext;
import de.chkal.togglz.core.manager.FeatureManager;

public class FeatureManagerProducer {

    @Produces
    @ApplicationScoped
    public FeatureManager produceFeatureManager() {
        return FeatureContext.getFeatureManager();
    }

}
