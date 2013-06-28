package org.togglz.core.metadata.enums;

import java.lang.annotation.Annotation;

import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;
import org.togglz.core.metadata.FeatureGroup;
import org.togglz.core.util.FeatureAnnotations;

/**
 * 
 * An implementation of {@link FeatureGroup} that based on annotations.
 * 
 * @author Christian Kaltepoth
 * 
 */
public class AnnotationFeatureGroup implements FeatureGroup {

    private final String label;
    private final Class<? extends Annotation> annotation;

    private AnnotationFeatureGroup(Class<? extends Annotation> groupAnnotation) {
        this.annotation = groupAnnotation;
        Label labelAnnotation = groupAnnotation.getAnnotation(Label.class);
        if (labelAnnotation != null) {
            label = labelAnnotation.value();
        } else {
            label = groupAnnotation.getClass().getSimpleName();
        }
    }

    public static FeatureGroup build(Class<? extends Annotation> groupAnnotation) {
        if (groupAnnotation.isAnnotationPresent(org.togglz.core.annotation.FeatureGroup.class)) {
            return new AnnotationFeatureGroup(groupAnnotation);
        }
        return null;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public boolean contains(Feature feature) {
        return FeatureAnnotations.isAnnotationPresent(feature, annotation);
    }

}
