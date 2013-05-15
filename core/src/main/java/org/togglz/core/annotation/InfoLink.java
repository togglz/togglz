package org.togglz.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * Custom feature attribute that allows to set a link for additional information regarding this feature.
 * 
 * @author Eli Abramovitch
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@FeatureAttribute("InfoLink")
public @interface InfoLink {

    /**
     * A link for additional information for this feature.
     */
    String value();

}
