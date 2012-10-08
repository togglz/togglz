package org.togglz.core.spi;

import org.togglz.core.logging.Log;
import org.togglz.core.util.Weighted;

/**
 * 
 * <p>
 * SPI for integrating with other logging frameworks.
 * </p>
 * 
 * <p>
 * Implementations and their weights:
 * </p>
 * 
 * <ul>
 * <li>Slf4jLogProvider: 100</li>
 * <li>Jdk14LogProvider: 1000</li>
 * </ul>
 * 
 * @author Christian Kaltepoth
 * 
 */
public interface LogProvider extends Weighted {

    Log getLog(String name);

}
