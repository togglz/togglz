package org.togglz.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.togglz.core.repository.StateRepository;
import org.togglz.core.spi.ActivationStrategy;

/**
 * 
 * Allows to specify the default activation strategy if the {@link StateRepository} doesn't have
 * any state saved for the feature.
 *
 * <pre>
 * public enum MyFeatures implements Feature {
 *
 *   &#064;DefaultActivationStrategy(
 *     id = UsernameActivationStrategy.ID,
 *     parameters = {
 *       &#064;ActivationParameter(name = UsernameActivationStrategy.PARAM_USERS, value = "person1,ck,person2")
 *     }
 *   )
 *   FEATURE_ONE;
 * }
 * </pre>
 *
 * @author Kai Hofstetter
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DefaultActivationStrategy {

    /**
     * The unique ID of the strategy. Corresponds to the value returned by
     * {@link ActivationStrategy#getId()}
     */
    String id();

    /**
     * Optional list of parameters to set for the strategy.
     */
    ActivationParameter[] parameters() default {};

}
