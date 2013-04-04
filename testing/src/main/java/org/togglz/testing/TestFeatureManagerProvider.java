package org.togglz.testing;

import org.togglz.core.manager.FeatureManager;
import org.togglz.core.spi.FeatureManagerProvider;

/**
 * 
 * This {@link FeatureManagerProvider} is used by the testing integration modules to provide a FeatureManager in unit tests.
 * 
 * @author Christian Kaltepoth
 * 
 */
public class TestFeatureManagerProvider implements FeatureManagerProvider {

    private static FeatureManager instance;

    @Override
    public int priority() {
        // very high priority
        return 10;
    }

    @Override
    public FeatureManager getFeatureManager() {
        return instance;
    }

    public static void setFeatureManager(FeatureManager instance) {
        TestFeatureManagerProvider.instance = instance;
    }

}
