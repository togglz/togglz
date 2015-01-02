package org.togglz.servlet.spi;

import javax.servlet.http.HttpServletRequest;

/**
 * This SPI can be implemented by modules to provide CSRF tokens that will be included
 * in all forms.
 */
public interface CSRFTokenProvider {

    /**
     * Returns the CSRF token or <code>null</code> if there is no token to include.
     */
    CSRFToken getToken(HttpServletRequest request);

}
