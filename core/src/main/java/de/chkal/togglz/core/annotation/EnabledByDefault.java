package de.chkal.togglz.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.chkal.togglz.core.repository.FeatureStateRepository;

/**
 * 
 * Allows to specify that the annotated feature should be enabled by default if the {@link FeatureStateRepository} doesn't have
 * any state saved.
 * 
 * @author Christian Kaltepoth
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EnabledByDefault {

}
