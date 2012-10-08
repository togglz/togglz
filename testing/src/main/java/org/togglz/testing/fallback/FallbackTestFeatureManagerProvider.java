package org.togglz.testing.fallback;

import org.togglz.core.manager.FeatureManager;
import org.togglz.core.spi.FeatureManagerProvider;

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
