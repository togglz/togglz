package org.togglz.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * Custom feature attribute that allows to set the name of a contact person.
 * 
 * @author Eli Abramovitch
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@FeatureAttribute("Owner")
public @interface Owner {

    /**
     * The name of a contact person for the feature.
     */
    String value();

}
