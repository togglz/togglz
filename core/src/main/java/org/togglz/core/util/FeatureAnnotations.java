package org.togglz.core.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.togglz.core.Feature;
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.annotation.Label;

/**
 * 
 * Utility class to read annotation on feature enums.
 * 
 * @author Christian Kaltepoth
 * 
 */
public class FeatureAnnotations {

    public static String getLabel(Feature feature) {
        Label label = getAnnotation(feature, Label.class);
        if (label != null) {
            return label.value();
        }
        return feature.name();
    }

    public static boolean isEnabledByDefault(Feature feature) {
        return isAnnotationPresent(feature, EnabledByDefault.class);
    }

    public static boolean isAnnotationPresent(Feature feature, Class<? extends Annotation> annotationType) {
        return getAnnotation(feature, annotationType) != null;
    }

    public static Annotation[] getAnnotations(Feature feature) {
        try {
            Field field = feature.getClass().getField(feature.name());
            return field.getAnnotations();
        } catch (SecurityException e) {
            // ignore
        } catch (NoSuchFieldException e) {
            // ignore
        }
        return null;
    }

    public static <A extends Annotation> A getAnnotation(Feature feature, Class<A> annotationType) {
        try {
            Field field = feature.getClass().getField(feature.name());
            return field.getAnnotation(annotationType);
        } catch (SecurityException e) {
            // ignore
        } catch (NoSuchFieldException e) {
            // ignore
        }
        return null;
    }

}
