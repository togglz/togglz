package org.togglz.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.togglz.core.activation.Parameter;

/**
 *
 * Allows to specify the activation parameters of the default activation strategy
 * {@link DefaultActivationStrategy}.
 * 
 * @author Kai Hofstetter
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ActivationParameter {

    /**
     * The name of the parameter. Corresponds to the value returned from {@link Parameter#getName()}.
     */
    String name();

    /**
     * The value of the parameter.
     */
    String value();

}
