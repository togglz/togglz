package de.chkal.togglz.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * Allows to set a label for a feature.
 * 
 * @author Christian Kaltepoth
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Label {

    /**
     * The human readable label of this feature
     * 
     * @return The label
     */
    String value();

}
