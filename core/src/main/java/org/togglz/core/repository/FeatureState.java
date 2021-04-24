package org.togglz.core.repository;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.togglz.core.Feature;
import org.togglz.core.activation.UsernameActivationStrategy;
import org.togglz.core.util.Strings;

/**
 * This class represents the state of a feature that is persisted by {@link StateRepository} implementations.
 *
 * @author Christian Kaltepoth
 */
public class FeatureState implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Feature feature;
    private boolean enabled;
    private String strategyId;
    private final Map<String, String> parameters = new HashMap<>();

    /**
     * This constructor creates a new feature state for the given feature. The feature is initially disabled if this constructor
     * is used.
     *
     * @param feature The feature that is represented by this state.
     */
    public FeatureState(Feature feature) {
        this(feature, false);
    }

    /**
     * This constructor creates a new feature state for the given feature.
     *
     * @param feature The feature that is represented by this state.
     * @param enabled boolean indicating whether this feature should be enabled or not.
     */
    public FeatureState(Feature feature, boolean enabled) {
        this.feature = feature;
        this.enabled = enabled;
    }

    /**
     * This constructor creates a new feature state for the given feature. Please not that using this constructor will
     * automatically set strategyId to match the {@link UsernameActivationStrategy}.
     *
     * @param feature The feature that is represented by this state.
     * @param enabled boolean indicating whether this feature should be enabled or not.
     * @param users A list of users
     *
     * @deprecated This constructor will be removed soon. You should use {@link #FeatureState(Feature, boolean)} and
     *             {@link #setParameter(String, String)} instead.
     */
    @Deprecated
    public FeatureState(Feature feature, boolean enabled, List<String> users) {
        this.feature = feature;
        this.enabled = enabled;
        this.strategyId = UsernameActivationStrategy.ID;
        this.addUsers(users);
    }

    /**
     * Creates a copy of this state object
     */
    public FeatureState copy() {
        FeatureState copy = new FeatureState(feature);
        copy.setEnabled(this.enabled);
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
     * Disable the feature
     */
    public FeatureState disable() {
        return setEnabled(false);
    }

    /**
     * The list of users associated with the feature state.
     *
     * @return The user list, never <code>null</code>
     *
     * @deprecated This method will be removed soon. Use {@link #getParameter(String)} instead to read the corresponding
     *             strategy parameter.
     */
    @Deprecated
    public List<String> getUsers() {
        String value = getParameter(UsernameActivationStrategy.PARAM_USERS);
        if (Strings.isNotBlank(value)) {
            return Strings.splitAndTrim(value, ",");
        }
        return Collections.emptyList();
    }

    /**
     * Adds a single user to the list of users
     *
     * @deprecated This method will be removed soon. Use {@link #setParameter(String, String)} instead to modify the
     *             corresponding strategy parameter.
     */
    @Deprecated
    public FeatureState addUser(String user) {
        return this.addUsers(Collections.singletonList(user));
    }

    /**
     * Adds a single user to the list of users
     *
     * @deprecated This method will be removed soon. Use {@link #setParameter(String, String)} instead to modify the
     *             corresponding strategy parameter.
     */
    @Deprecated
    public FeatureState addUsers(Collection<String> users) {
        Set<String> set = new LinkedHashSet<>();
        set.addAll(this.getUsers());
        set.addAll(users);
        String setAsString = Strings.trimToNull(Strings.join(set, ","));
        setParameter(UsernameActivationStrategy.PARAM_USERS, setAsString);
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
    public FeatureState setStrategyId(String strategyId) {
        this.strategyId = strategyId;
        return this;
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
        if (value != null) {
            this.parameters.put(name, value);
        }
        else {
            this.parameters.remove(name);
        }
        return this;
    }

    /**
     * Returns a list of all parameter names stored in the {@link FeatureState} instance.
     */
    public Set<String> getParameterNames() {
        return this.parameters.keySet();
    }

    /**
     * Returns an unmodifiable map of parameters
     */
    public Map<String, String> getParameterMap() {
        return Collections.unmodifiableMap(this.parameters);
    }

    /**
     * Returns a copy of a featureState, or <code>null</code> if the featureState is
     * <code>null</code>.
     */
    public static FeatureState copyOf(FeatureState featureState) {
        return featureState == null ? null : featureState.copy();
    }
}
