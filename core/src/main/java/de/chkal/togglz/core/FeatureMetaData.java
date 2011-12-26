package de.chkal.togglz.core;

import de.chkal.togglz.core.util.FeatureAnnotations;

public class FeatureMetaData {

    private final String label;
    private final boolean enabledByDefault;

    public FeatureMetaData(Feature feature) {
        this.label = FeatureAnnotations.getLabel(feature);
        this.enabledByDefault = FeatureAnnotations.isEnabledByDefault(feature);
    }

    public String getLabel() {
        return label;
    }

    public boolean isEnabledByDefault() {
        return enabledByDefault;
    }

}
