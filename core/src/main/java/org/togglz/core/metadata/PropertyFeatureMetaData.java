package org.togglz.core.metadata;

import java.util.HashSet;
import java.util.Set;

import org.togglz.core.Feature;
import org.togglz.core.group.FeatureGroup;
import org.togglz.core.group.SimpleFeatureGroup;
import org.togglz.core.manager.PropertyFeatureProvider;
import org.togglz.core.util.Strings;

/**
 * Metadata used by {@link PropertyFeatureProvider}.
 */
public class PropertyFeatureMetaData implements FeatureMetaData {

    private String label;
    private boolean enabledByDefault = false;
    private final Set<FeatureGroup> groups = new HashSet<FeatureGroup>();

    public PropertyFeatureMetaData(Feature feature, String specification) {

        if (Strings.isNotBlank(specification)) {
            String[] parts = specification.split(";");

            if (parts.length >= 1) {
                label = parts[0];
            }

            if (parts.length >= 2) {
                enabledByDefault = Boolean.parseBoolean(parts[1]);
            }

            if (parts.length >= 3) {
                groups.addAll(parseFeatureGroups(parts[2]));
            }

        }

        if (Strings.isBlank(label)) {
            label = feature.name();
        }

    }

    private Set<FeatureGroup> parseFeatureGroups(String value) {
        Set<FeatureGroup> groups = new HashSet<FeatureGroup>();
        for (String label : value.split(",")) {
            if (Strings.isNotBlank(label)) {
                groups.add(new SimpleFeatureGroup(label.trim()));
            }
        }
        return groups;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public boolean isEnabledByDefault() {
        return enabledByDefault;
    }

    @Override
    public Set<FeatureGroup> getGroups() {
        return groups;
    }

}
