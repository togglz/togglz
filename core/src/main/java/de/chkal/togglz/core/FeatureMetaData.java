package de.chkal.togglz.core;

import de.chkal.togglz.core.annotation.EnabledByDefault;
import de.chkal.togglz.core.annotation.Label;
import de.chkal.togglz.core.util.FeatureAnnotations;

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
