package org.togglz.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * Identifies an annotation type as an feature group annotation
 * 
 * @author Bennet Schulz
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.TYPE })
public @interface FeatureGroup {

    /**
     * The human readable feature group of this feature
     *
     * @return The feature group
     */
    String value() default "";
}
