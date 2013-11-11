package org.togglz.core.context;

import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.spi.FeatureManagerProvider;

/**
 * Implementation of {@link FeatureManagerProvider} for Java SE applications. To use it, create a {@link FeatureManager} using
 * the {@link FeatureManagerBuilder} and register it using {@link #setFeatureManager(FeatureManager)}.
 * 
 * @author Christian Kaltepoth
 */
public class StaticFeatureManagerProvider implements FeatureManagerProvider {

    private static volatile FeatureManager staticInstance = null;

    @Override
    public int priority() {
        return 70;
    }

    @Override
    public FeatureManager getFeatureManager() {
        return staticInstance;
    }

    /**
     * Sets the {@link FeatureManager} that the provider should return for calls to {@link #getFeatureManager()}.
     */
    public static void setFeatureManager(FeatureManager featureManager) {
        staticInstance = featureManager;
    }

}
