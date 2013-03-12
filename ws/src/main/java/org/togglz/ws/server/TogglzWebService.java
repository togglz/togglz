package org.togglz.ws.server;

import javax.jws.WebService;

/**
 * Web service endpoint interface to Togglz.
 * 
 * @author Mauro Talevi
 */
@WebService
public interface TogglzWebService {
   
    /**
     * Determines if a feature is active
     * 
     * @param name the feature name
     * @return A boolen flag, <code>true> if active
     */
    boolean isFeatureActive(String name);
    
}

