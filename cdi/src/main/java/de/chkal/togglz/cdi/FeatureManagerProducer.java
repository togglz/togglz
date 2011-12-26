package de.chkal.togglz.cdi;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import de.chkal.togglz.core.manager.FeatureManager;

public class FeatureManagerProducer {

    @Inject
    private ServletContext servletContext;

    @ApplicationScoped
    public FeatureManager produceFeatureManager() {
        return (FeatureManager) servletContext.getAttribute(FeatureManager.class.getName());
    }

}
