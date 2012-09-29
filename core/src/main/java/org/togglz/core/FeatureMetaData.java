package org.togglz.core;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.annotation.Label;
import org.togglz.core.group.AnnotationFeatureGroup;
import org.togglz.core.group.FeatureGroup;
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
    private final Set<FeatureGroup> groups = new HashSet<FeatureGroup>();

    private FeatureMetaData(Feature feature) {
        this.label = FeatureAnnotations.getLabel(feature);
        this.enabledByDefault = FeatureAnnotations.isEnabledByDefault(feature);
        for (Annotation annotation : FeatureAnnotations.getAnnotations(feature)) {
            FeatureGroup group = AnnotationFeatureGroup.build(annotation.annotationType());
            if (group != null) {
                groups.add(group);
            }
        }
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

    public Set<FeatureGroup> getGroups() {
        return groups;
    }

}
