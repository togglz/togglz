package org.togglz.core.spi;

import org.togglz.core.logging.Log;
import org.togglz.core.util.Weighted;

/**
 * 
 * SPI for integrating with other logging frameworks.
 * 
 * @author Christian Kaltepoth
 * 
 */
public interface LogProvider extends Weighted {

    Log getLog(String name);

}
