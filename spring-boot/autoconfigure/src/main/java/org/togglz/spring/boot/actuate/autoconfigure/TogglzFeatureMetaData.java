package org.togglz.spring.boot.actuate.autoconfigure;

import org.togglz.core.metadata.FeatureGroup;
import org.togglz.core.metadata.FeatureMetaData;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Data Transfer Object for Togglz feature metadata.
 *
 * @author Peter Triller
 */
public class TogglzFeatureMetaData {

    private final String label;

    private final Set<String> groups;

    private final boolean enabledByDefault;

    private final Map<String, String> attributes;

    public TogglzFeatureMetaData(FeatureMetaData metaData) {
        this.label = metaData.getLabel();
        this.groups = metaData.getGroups().stream()
                .map(FeatureGroup::getLabel)
                .collect(Collectors.toSet());
        this.enabledByDefault = metaData.getDefaultFeatureState().isEnabled();
        this.attributes = new HashMap<>(metaData.getAttributes());
    }

    public String getLabel() {
        return label;
    }

    public Set<String> getGroups() {
        return groups;
    }

    public boolean isEnabledByDefault() {
        return enabledByDefault;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }
}
