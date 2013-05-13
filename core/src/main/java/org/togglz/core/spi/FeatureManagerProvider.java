package org.togglz.core.spi;

import org.togglz.core.context.FeatureContext;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.util.Weighted;

/**
 * <p>
 * SPI used by {@link FeatureContext} to lookup the {@link FeatureManager} to use.
 * </p>
 * 
 * <p>
 * Implementations and their weights:
 * </p>
 * 
 * <ul>
 * <li>TestFeatureManagerProvider: 10</li>
 * <li>FallbackTestFeatureManagerProvider: 20</li>
 * <li>ThreadLocalFeatureManagerProvider: 50</li>
 * <li>BeanFinderFeatureManagerProvider: 60</li>
 * <li>StaticFeatureManagerProvider: 70</li>
 * <li>WebAppFeatureManagerProvider: 100</li>
 * <li>JNDIFeatureManagerProvider: 200</li>
 * </ul>
 * 
 * @author Christian Kaltepoth
 * 
 */
public interface FeatureManagerProvider extends Weighted {

    FeatureManager getFeatureManager();

}
