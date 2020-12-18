package org.togglz.servlet.spi;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.togglz.core.util.Weighted;
import org.togglz.servlet.TogglzFilter;

/**
 * 
 * SPI for components that want to be notified about requests.
 * 
 * @author Christian Kaltepoth
 * 
 */
public interface RequestListener extends Weighted {

    /**
     * Called before the {@link TogglzFilter} processes a request.
     */
    void begin(HttpServletRequest request, HttpServletResponse response);

    /**
     * Called after the {@link TogglzFilter} processed a request.
     */
    void end(HttpServletRequest request, HttpServletResponse response);

}
