package org.togglz.junit5;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;
import org.togglz.core.Feature;

/**
 * <p>
 *     Creates a {@link org.togglz.testing.TestFeatureManager TestFeatureManager} with all features enabled.
 * </p>
 *
 * <p>
 *     To disable single features the {@link org.togglz.testing.TestFeatureManager TestFeatureManager}
 *     is available as parameter.
 * </p>
 *
 * <p>
 *     Example Usage:
 * </p>
 * <pre>
 *     class MyTest {
 *
 *         &#064;Test
 *         &#064;AllEnabled(MyFeatures.class)
 *         void run(TestFeatureManager featureManager) {
 *             assertTrue(featureManager.isActive(MyFeatures.ONE));
 *
 *             featureManager.disable(MyFeatures.ONE);
 *             assertFalse(featureManager.isActive(MyFeatures.ONE));
 *         }
 *     }
 * </pre>
 *
 * @see AllDisabled
 *
 * @author Roland Weisleder
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Inherited
@ExtendWith(AnnotationBasedTogglzExtension.class)
public @interface AllEnabled {

    Class<? extends Feature> value();

}
