package org.togglz.core.user;

import org.togglz.core.manager.DefaultFeatureManager;

/**
 * 
 * Default implementation of {@link FeatureUser}.
 * 
 * @author Christian Kaltepoth
 * 
 */
public class SimpleFeatureUser implements FeatureUser {

    private final String name;
    private final boolean featureAdmin;

    /**
     * Constructor of {@link DefaultFeatureManager}.
     * 
     * @param name The name of the user
     * @param featureAdmin <code>true</code> if the user is a feature admin
     */
    public SimpleFeatureUser(String name, boolean featureAdmin) {
        this.name = name;
        this.featureAdmin = featureAdmin;
    }

    public String getName() {
        return name;
    }

    public boolean isFeatureAdmin() {
        return featureAdmin;
    }

}
