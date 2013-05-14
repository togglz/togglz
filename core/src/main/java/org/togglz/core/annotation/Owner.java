package org.togglz.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * Allows to set the name of the contact person for this feature.
 * 
 * @author Eli Abramovitch
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.TYPE })
public @interface Owner {

    /**
     * The name of the contact person for the feature.
     */
    String value();

}
