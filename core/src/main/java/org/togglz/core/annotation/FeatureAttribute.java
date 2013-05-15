package org.togglz.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Annotation used to define custom metadata attributes for features.
 * </p>
 * 
 * <p>
 * Let's assume you want to define a custom attribute that holds the issue identifier of your issue tracker. To do so, create an
 * annotation like this:
 * </p>
 * 
 * <pre>
 * &#064;Retention(RetentionPolicy.RUNTIME)
 * &#064;Target(ElementType.FIELD)
 * &#064;FeatureAttribute(&quot;Issue&quot;)
 * public @interface Issue {
 *     String value();
 * }
 * </pre>
 * 
 * <p>
 * Now you can use this new annotation on your feature enum like this:
 * </p>
 * 
 * <pre>
 * public enum MyFeatures implements Feature {
 * 
 *     &#064;Label(&quot;My cool new feature&quot;)
 *     &#064;Issue(&quot;TOGGLZ-123&quot;)
 *     MY_NEW_FEATURE;
 * 
 * }
 * </pre>
 * 
 * @author Christian Kaltepoth
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FeatureAttribute {

    /**
     * The name of the feature attribute.
     */
    String value();

    /**
     * The name of the method to call on the annotation to retrieve the value of the attribute.
     */
    String annotationAttribute() default "value";

}
