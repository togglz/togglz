package org.togglz.core.user;

import java.util.HashMap;
import java.util.Map;

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
    private final Map<String, Object> attributes = new HashMap<>();

    /**
     * Constructor of {@link DefaultFeatureManager}. The <code>featureAdmin</code> flag will be set to <code>false</code>.
     * 
     * @param name The name of the user
     */
    public SimpleFeatureUser(String name) {
        this(name, false);
    }

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

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isFeatureAdmin() {
        return featureAdmin;
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    /**
     * This method can be used to set attributes of the user.
     * 
     * @param name The name of the attribute
     * @param value The value of the attribute
     * @return <code>this</code> for fluent object creation
     */
    public SimpleFeatureUser setAttribute(String name, Object value) {
        if (value != null) {
            attributes.put(name, value);
        } else {
            attributes.remove(name);
        }
        return this;
    }

}
