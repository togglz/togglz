package org.togglz.junit;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.togglz.core.Feature;

/**
 * Used together with the {@link TogglzRule} on test methods.
 */
@Target({ METHOD })
@Retention(RUNTIME)
public @interface WithFeature
{

    /**
     * @deprecated Removed without replacement
     */
    @Deprecated
    Class<? extends Feature> type() default Feature.class;

    /**
     * The features to enable
     */
    String[] value();

    /**
     * disable the features instead of enabling them
     */
    boolean disable() default false;

}
