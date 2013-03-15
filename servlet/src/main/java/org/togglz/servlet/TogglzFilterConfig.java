package org.togglz.servlet;

import javax.servlet.ServletContext;

import org.togglz.core.manager.FeatureManager;

/**
 * Encapsulates the configuration provided by the various web context parameters.
 * 
 * @author Christian Kaltepoth
 */
class TogglzFilterConfig {

    private static final String LOCAL_FEATURE_MANAGER = "org.togglz.LOCAL_FEATURE_MANAGER";

    private final ServletContext servletContext;

    public TogglzFilterConfig(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * Returns <code>true</code> if the filter should create a local {@link FeatureManager} for the web application.
     */
    public boolean isCreateLocalFeatureManager() {
        return !isFalse(servletContext.getInitParameter(LOCAL_FEATURE_MANAGER));
    }

    private static boolean isFalse(String value) {
        return value != null && "false".equalsIgnoreCase(value.trim());
    }
}
