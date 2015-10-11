package org.togglz.core.metadata.property;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.togglz.core.Feature;
import org.togglz.core.manager.PropertyFeatureProvider;
import org.togglz.core.metadata.FeatureGroup;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.metadata.SimpleFeatureGroup;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.util.Strings;

/**
 * Metadata used by {@link PropertyFeatureProvider}.
 */
public class PropertyFeatureMetaData implements FeatureMetaData {

    private String label;
    private final FeatureState defaultFeatureState;
    private final Set<FeatureGroup> groups = new HashSet<FeatureGroup>();

    public PropertyFeatureMetaData(Feature feature, String specification) {
        boolean enabledByDefault = false;

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

        defaultFeatureState = new FeatureState(feature, enabledByDefault);

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
    public FeatureState getDefaultFeatureState() {
        return defaultFeatureState;
    }

    @Override
    public Set<FeatureGroup> getGroups() {
        return groups;
    }

    @Override
    public Map<String, String> getAttributes() {
        // currently not supported by this implementation
        return Collections.emptyMap();
    }

}
