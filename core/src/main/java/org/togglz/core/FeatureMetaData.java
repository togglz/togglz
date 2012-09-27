package org.togglz.core;

import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.annotation.Label;
import org.togglz.core.util.FeatureAnnotations;

/**
 * 
 * Represents metadata of a feature that can be specified using annotations like {@link Label} or {@link EnabledByDefault}.
 * 
 * @author Christian Kaltepoth
 * 
 */
public class FeatureMetaData {

    private final String label;
    private final boolean enabledByDefault;

    private FeatureMetaData(Feature feature) {
        this.label = FeatureAnnotations.getLabel(feature);
        this.enabledByDefault = FeatureAnnotations.isEnabledByDefault(feature);
    }

    public static FeatureMetaData build(Feature feature) {
        return new FeatureMetaData(feature);
    }

    public String getLabel() {
        return label;
    }

    public boolean isEnabledByDefault() {
        return enabledByDefault;
    }

}
