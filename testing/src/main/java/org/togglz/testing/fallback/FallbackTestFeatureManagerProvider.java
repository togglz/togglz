package org.togglz.testing.fallback;

import org.togglz.core.manager.FeatureManager;
import org.togglz.core.spi.FeatureManagerProvider;

/**
 * This {@link FeatureManagerProvider} is a fallback for unit tests. It will return a {@link FallbackTestFeatureManager} which
 * will simply enable all features.
 * 
 * @author Christian Kaltepoth
 */
public class FallbackTestFeatureManagerProvider implements FeatureManagerProvider {

    @Override
    public int priority() {
        return 20;
    }

    @Override
    public FeatureManager getFeatureManager() {
        return new FallbackTestFeatureManager();
    }

}
