package org.togglz.core.annotation;

import org.togglz.core.repository.StateRepository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 
 * Allows to specify the activation parameters of the default activation strategy {@link DefaultActivationStrategy}.
 * 
 * @author Kai Hofstetter
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ActivationParameter {
    String name();
    String value();
}
