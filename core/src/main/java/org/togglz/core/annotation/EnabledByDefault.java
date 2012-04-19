package org.togglz.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.togglz.core.repository.StateRepository;


/**
 * 
 * Allows to specify that the annotated feature should be enabled by default if the {@link StateRepository} doesn't have
 * any state saved.
 * 
 * @author Christian Kaltepoth
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EnabledByDefault {

}
