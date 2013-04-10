package org.togglz.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * Allows to set an owner for a feature.
 * 
 * @author Eli Abramovitch
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.TYPE })
public @interface Owner {

    /**
     * The human readable owner of this feature
     * 
     * @return The feature owner
     */
    String value();

}
