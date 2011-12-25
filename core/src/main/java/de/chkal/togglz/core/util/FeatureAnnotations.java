package de.chkal.togglz.core.util;

import java.lang.reflect.Field;

import de.chkal.togglz.core.Feature;
import de.chkal.togglz.core.annotation.EnabledByDefault;
import de.chkal.togglz.core.annotation.Label;

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
