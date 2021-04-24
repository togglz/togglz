package org.togglz.core.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.togglz.core.Feature;
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.annotation.FeatureAttribute;
import org.togglz.core.annotation.InfoLink;
import org.togglz.core.annotation.Label;
import org.togglz.core.annotation.Owner;

/**
 * Utility class to read annotation on feature enums.
 *
 * @author Christian Kaltepoth
 * @author Eli Abramovitch
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

    public static Set<Annotation> getAnnotations(Feature feature) {
        Set<Annotation> annotations = new HashSet<>();
        try {
            Class<? extends Feature> featureClass = feature.getClass();
            Annotation[] fieldAnnotations = featureClass.getField(feature.name()).getAnnotations();
            Annotation[] classAnnotations = featureClass.getAnnotations();

            annotations.addAll(Arrays.asList(fieldAnnotations));
            annotations.addAll(Arrays.asList(classAnnotations));

            return annotations;
        } catch (SecurityException | NoSuchFieldException e) {
            // ignore
        }
        return annotations;
    }

    public static <A extends Annotation> A getAnnotation(Feature feature, Class<A> annotationType) {
        try {
            Class<? extends Feature> featureClass = feature.getClass();
            A fieldAnnotation = featureClass.getField(feature.name()).getAnnotation(annotationType);
            A classAnnotation = featureClass.getAnnotation(annotationType);

            return fieldAnnotation != null ? fieldAnnotation : classAnnotation;
        } catch (SecurityException | NoSuchFieldException e) {
            // ignore
        }
        return null;
    }

    /**
     * Checks whether the supplied annotation specifies a feature attribute. If so, it returns an String array containing the
     * name of the attribute at the first and the value at the second position. Returns <code>null</code> if no attribute was
     * found.
     */
    public static String[] getFeatureAttribute(Annotation annotation) {
        try {
            // only annotations which are annotated with @FeatureAttribute are interesting
            FeatureAttribute details = annotation.annotationType().getAnnotation(FeatureAttribute.class);
            if (details != null) {

                // this is the name of the feature attribute
                String attributeName = details.value();

                // find the method to invoke on the annotation to read the value of the feature attribute
                Method method = annotation.getClass().getMethod(details.annotationAttribute());
                if (method != null) {
                    String attributeValue = method.invoke(annotation).toString();
                    return new String[]{attributeName, attributeValue};
                }

            }
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
        return null;
    }
}
