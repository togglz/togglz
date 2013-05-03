package org.togglz.core.group;

import org.togglz.core.Feature;
import org.togglz.core.manager.PropertyBasedFeatureProvider;

/**
 * Feature group implementation used by {@link PropertyBasedFeatureProvider}.
 */
public class PropertyBasedFeatureGroup implements FeatureGroup {

    private final String label;
    private final String featureName;

    public PropertyBasedFeatureGroup(String label, String featureName) {
        this.label = label;
        this.featureName = featureName;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public boolean contains(Feature feature) {
        return feature.name().equals(featureName);
    }

}