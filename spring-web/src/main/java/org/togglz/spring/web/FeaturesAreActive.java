package org.togglz.spring.web;

import org.springframework.stereotype.Controller;
import org.togglz.core.Feature;

import java.lang.annotation.*;

/**
 * Annotate a {@link Controller} or a controller method to only activate it when
 * all the features given in the {@link #features()} attribute of the given {@link #featureClass()} are active.
 * <p>
 * If the features are not activated, a response with the status code given by the {@link #responseStatus()}
 * attribute is generated (404 by default).
 * <p>
 * The {@link Feature} implementation given in the {@link #featureClass()} attribute needs to be an enum.
 * 
 * <pre>
 * &#64;Controller
 * &#64;RequestMapping("/new-feature")
 * &#64;FeaturesAreActive(featureClass=MyFeatures.class, features="NEW_FEATURE")
 * public class MyNewFeature {
 *     &#64;RequestMapping(method = RequestMethod.GET)
 *     public String newFeature() {
 *         return ....;
 *     }
 *     
 *     &#64;FeaturesAreActive(featureClass=MyFeatures.class, features="NEW_SECURE_FEATURE", responseStatus=403)
 *     &#64;RequestMapping(value="/secure", method = RequestMethod.GET)
 *     public String newSecureFeature() {
 *         return ....;
 *     }
 * }
 * </pre>
 * The {@link FeatureInterceptor} needs to be registered in Spring MVC for this to work. This is automatically
 * done by the TogglzAutoConfiguration config which is loaded if @EnableAutoConfiguration
 * is used in your project.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface FeaturesAreActive {
	String[] features();
	Class<? extends Feature> featureClass();
	int responseStatus() default 404;
}
