package org.togglz.ws.server;

import java.util.NoSuchElementException;

import javax.jws.WebService;

import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.logging.Log;
import org.togglz.core.logging.LogFactory;
import org.togglz.core.manager.FeatureManager;

/**
 * Web service implementation of endpoint {@link TogglzWebService} which uses
 * the {@link FeatureManager} retrieved from the {@link FeatureContext}.
 * 
 * @author Mauro Talevi
 */
@WebService(endpointInterface = "org.togglz.ws.server.TogglzWebService", serviceName = "TogglzWebService")
public class ContextTogglzWebService implements TogglzWebService {

    private final Log log = LogFactory.getLog(ContextTogglzWebService.class);

    public boolean isFeatureActive(String name) {
        FeatureManager manager = FeatureContext.getFeatureManager();
        try {
            boolean active = manager.isActive(feature(manager, name));
            log.debug("Feature " + name + " is " + (!active ? "not" : "") + " active");
            return active;
        } catch (NoSuchElementException e) {
            log.debug("Feature " + name + " is not found");
            return false;
        }
    }

    private Feature feature(FeatureManager manager, String name) {
        for (Feature feature : manager.getFeatures()) {
            if (name.equals(feature.name())) {
                return feature;
            }
        }
        throw new NoSuchElementException(name);
    }

}
