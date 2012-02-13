package org.togglz.core.util;

import java.lang.reflect.Field;

import org.togglz.core.Feature;
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.annotation.Label;


public class FeatureAnnotations {

    public static String getLabel(Feature feature) {
        try {
            Field field = feature.getClass().getField(feature.name());
            Label annotation = field.getAnnotation(Label.class);
            if (annotation != null) {
                return annotation.value();
            }
        } catch (SecurityException e) {
            // ignore
        } catch (NoSuchFieldException e) {
            // ignore
        }
        return feature.name();
    }

    public static boolean isEnabledByDefault(Feature feature) {
        try {
            Field field = feature.getClass().getField(feature.name());
            if (field.getAnnotation(EnabledByDefault.class) != null) {
                return true;
            }
        } catch (SecurityException e) {
            // ignore
        } catch (NoSuchFieldException e) {
            // ignore
        }
        return false;
    }

}
