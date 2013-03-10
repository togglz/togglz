package org.togglz.ws.server;

import javax.jws.WebService;

/**
 * Web service endpoint interface that determines if feature is active.
 * 
 * @author Mauro Talevi
 */
@WebService
public interface FeatureActivity {
    
    boolean isActive(String name);
    
}

