package org.togglz.servlet;

import jakarta.servlet.ServletContext;

/**
 * Encapsulates the configuration provided by the various web context parameters.
 *
 * @author Christian Kaltepoth
 */
class TogglzFilterConfig {

    private final ServletContext servletContext;

    public TogglzFilterConfig(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * Returns <code>true</code> if the filter should perform the bootstrapping process. Returns <code>null</code> if the user
     * didn't specify what to do.
     */
    public Boolean isPerformBootstrap() {
        String managerProvided = servletContext.getInitParameter("org.togglz.FEATURE_MANAGER_PROVIDED");
        if (managerProvided != null) {
            return !toBool(managerProvided);
        }
        return null;
    }

    private static boolean toBool(String value) {
        if (value != null && "true".equalsIgnoreCase(value.trim())) {
            return true;
        }
        if (value != null && "false".equalsIgnoreCase(value.trim())) {
            return false;
        }
        throw new IllegalArgumentException("Not a valid boolean value: " + value);
    }
}
