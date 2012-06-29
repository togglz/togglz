package org.togglz.core.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.togglz.core.Feature;


/**
 * This class represents the state of a feature that is persisted by the {@link StateRepository} implementations.
 * 
 * @author Christian Kaltepoth
 * 
 */
public class FeatureState {

    private final Feature feature;
    private final boolean enabled;
    private final List<String> users;

    /**
     * This constructor creates a new feature state with an empty user list.
     * 
     * @param feature The feature that is represented by this object.
     * @param enabled Flag indicating whether this feature is enabled or not.
     */
    public FeatureState(Feature feature, boolean enabled) {
        this(feature, enabled, new ArrayList<String>());
    }

    /**
     * Creates a new {@link FeatureState} instance.
     * 
     * @param feature The feature that is represented by this object.
     * @param enabled Flag indicating whether this feature is enabled or not.
     * @param users A list of users
     */
    public FeatureState(Feature feature, boolean enabled, List<String> users) {
        this.feature = feature;
        this.enabled = enabled;
        this.users = users;
    }

    /**
     * Creates a copy of this object
     */
    public FeatureState copy() {
        return new FeatureState(feature, enabled, new ArrayList<String>(users));
    }

    /**
     * Returns the feature represented by this feature state.
     * 
     * @return The feature, never <code>null</code>
     */
    public Feature getFeature() {
        return feature;
    }

    /**
     * Whether this feature is enabled or not.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * The list of users associated with the feature state.
     * 
     * @return The user list, never <code>null</code>
     */
    public List<String> getUsers() {
        return Collections.unmodifiableList(users);
    }
}
