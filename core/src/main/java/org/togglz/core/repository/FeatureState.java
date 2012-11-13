package org.togglz.core.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.togglz.core.Feature;

/**
 * This class represents the state of a feature that is persisted by the {@link StateRepository} implementations.
 * 
 * @author Christian Kaltepoth
 * 
 */
public class FeatureState {

    private final Feature feature;
    private boolean enabled;

    private final List<String> users;

    private String strategyId;
    private final Map<String, String> parameters = new HashMap<String, String>();

    /**
     * This constructor creates a new feature state with an empty user list.
     * 
     * @param feature The feature that is represented by this object.
     */
    public FeatureState(Feature feature) {
        this(feature, false);
    }

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
        FeatureState copy = new FeatureState(feature);
        copy.setEnabled(this.enabled);
        copy.addUsers(this.users);
        copy.setStrategyId(this.strategyId);
        for (Entry<String, String> entry : this.parameters.entrySet()) {
            copy.setParameter(entry.getKey(), entry.getValue());
        }
        return copy;
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
     * Enables or disables the feature.
     */
    public FeatureState setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Enable the feature
     */
    public FeatureState enable() {
        return setEnabled(true);
    }

    /**
     * Enable the feature
     */
    public FeatureState disable() {
        return setEnabled(false);
    }

    /**
     * The list of users associated with the feature state.
     * 
     * @return The user list, never <code>null</code>
     */
    public List<String> getUsers() {
        return Collections.unmodifiableList(users);
    }

    /**
     * Adds a single user to the list of users
     */
    public FeatureState addUser(String user) {
        this.users.add(user);
        return this;
    }

    /**
     * Adds a single user to the list of users
     */
    public FeatureState addUsers(Collection<String> users) {
        this.users.addAll(users);
        return this;
    }

    /**
     * Returns the ID of the selected activation strategy.
     */
    public String getStrategyId() {
        return strategyId;
    }

    /**
     * Sets the selected activation strategy ID
     */
    public void setStrategyId(String strategyId) {
        this.strategyId = strategyId;
    }

    /**
     * Returns the value of the given parameter. May return <code>null</code>.
     */
    public String getParameter(String name) {
        return this.parameters.get(name);
    }

    /**
     * Sets a new value for the given parameter.
     */
    public FeatureState setParameter(String name, String value) {
        this.parameters.put(name, value);
        return this;
    }

    /**
     * Returns a list of all parameter names stored in the {@link FeatureState} instance.
     */
    public Set<String> getParameterNames() {
        return this.parameters.keySet();
    }

}
