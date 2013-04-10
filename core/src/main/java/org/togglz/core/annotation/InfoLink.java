package org.togglz.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * Allows to set a link for information on this feature.
 * 
 * @author Eli Abramovitch
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.TYPE })
public @interface InfoLink {

    /**
     *
     * 
     * @return The link for information on this feature
     */
    String value();

}
