package de.chkal.togglz.core.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.chkal.togglz.core.Feature;

public class FeatureState {

    private final Feature feature;
    private final boolean enabled;
    private final List<String> users;

    public FeatureState(Feature feature) {
        this(feature, false);
    }

    public FeatureState(Feature feature, boolean enabled) {
        this(feature, enabled, new ArrayList<String>());
    }

    public FeatureState(Feature feature, boolean enabled, List<String> users) {
        this.feature = feature;
        this.enabled = enabled;
        this.users = users;
    }

    public Feature getFeature() {
        return feature;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<String> getUsers() {
        return Collections.unmodifiableList(users);
    }
}
