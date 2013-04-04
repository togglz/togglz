package org.togglz.core.spi;

import org.togglz.core.manager.FeatureManager;
import org.togglz.core.util.Weighted;

/**
 * 
 * Implementations of this SPI will be notified when the {@link FeatureManager} is created and before it is shut down.
 * 
 * @author Christian Kaltepoth
 * 
 */
public interface FeatureManagerListener extends Weighted {

    /**
     * Called after the {@link FeatureManager} for the application has been created.
     */
    void start(FeatureManager featureManager);

    /**
     * Called before the {@link FeatureManager} of the application is destroyed.
     */
    void stop(FeatureManager featureManager);

}
