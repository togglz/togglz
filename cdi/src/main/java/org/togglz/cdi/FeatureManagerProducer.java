package org.togglz.cdi;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.togglz.core.context.FeatureContext;
import org.togglz.core.manager.FeatureManager;

public class FeatureManagerProducer {

    @Produces
    @ApplicationScoped
    public FeatureManager produceFeatureManager() {
        return FeatureContext.getFeatureManager();
    }

}
