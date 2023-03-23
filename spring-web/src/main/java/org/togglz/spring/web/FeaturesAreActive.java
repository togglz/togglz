package org.togglz.spring.web;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a {@link Controller} or a controller method to only activate it when
 * all the features given in the {@link #features()} attribute are active.
 * <p>
 * If the features are not activated, a response with the status code given by the {@link #errorResponseStatus()}
 * attribute is generated (HttpStatus.NOT_FOUND (404) by default).
 *
 * <pre>
 * @Controller
 * @RequestMapping("/new-feature")
 * @FeaturesAreActive(features="NEW_FEATURE")
 * public class MyNewFeature {
 *     @RequestMapping(method = RequestMethod.GET)
 *     public String newFeature() {
 *         return ....;
 *     }
 *
 *     @FeaturesAreActive(features="NEW_SECURE_FEATURE", errorResponseStatus = HttpStatus.FORBIDDEN)
 *     @RequestMapping(value="/secure", method = RequestMethod.GET)
 *     public String newSecureFeature() {
 *         return ....;
 *     }
 * }
 * </pre>
 * The {@link FeatureInterceptor} needs to be registered in Spring MVC for this to work. This is automatically
 * done by the TogglzAutoConfiguration config which is loaded if @EnableAutoConfiguration
 * is used in your project.
 *
 * @author ractive
 * @author m-schroeer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface FeaturesAreActive {

    HttpStatus DEFAULT_ERROR_RESPONSE_STATUS = HttpStatus.NOT_FOUND;

    String[] features();

    HttpStatus errorResponseStatus() default HttpStatus.NOT_FOUND;
}
