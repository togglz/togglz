package org.togglz.core.spi;

import org.togglz.core.context.FeatureContext;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.util.Weighted;

/**
 * 
 * SPI used by {@link FeatureContext} to lookup the {@link FeatureManager} to use.
 * 
 * @author Christian Kaltepoth
 * 
 */
public interface FeatureManagerProvider extends Weighted {

    FeatureManager getFeatureManager();

}
