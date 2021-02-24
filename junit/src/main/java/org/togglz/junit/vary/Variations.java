package org.togglz.junit.vary;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.togglz.testing.vary.VariationSetBuilder;

/**
 * This annotation is used if a test class is executed with {@link FeatureVariations}. The method annotated with this annotation
 * must be public static and must return a {@link VariationSetBuilder}.
 * 
 * @see FeatureVariations
 * 
 * @author Christian Kaltepoth
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Variations {

}
