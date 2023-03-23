package org.togglz.junit;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Used together with the {@link TogglzRule} on test methods.
 */
@Target({ METHOD })
@Retention(RUNTIME)
public @interface WithFeature
{
    /**
     * The features to enable
     */
    String[] value();

    /**
     * disable the features instead of enabling them
     */
    boolean disable() default false;
}
