package org.togglz.console.handlers.index;

import org.togglz.core.FeatureMetaData;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.util.Strings;

public class IndexPageRow {

    private final String name;

    private final String label;

    private final boolean enabled;

    private final String users;

    public IndexPageRow(FeatureState state) {
        this.name = state.getFeature().name();
        this.label = FeatureMetaData.build(state.getFeature()).getLabel();
        this.enabled = state.isEnabled();
        this.users = Strings.join(state.getUsers(), ", ");
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getUsers() {
        return users;
    }

}