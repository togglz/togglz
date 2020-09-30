package org.togglz.junit5;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <p>
 *     Provides a {@link org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider TestTemplateInvocationContextProvider}
 *     to run {@link org.junit.jupiter.api.TestTemplate TestTemplates} with a variation of enabled and disabled features.
 * </p>
 *
 * <p>
 *     Example Usage:
 * </p>
 * <pre>
 *     class PermutationTests {
 *
 *         &#064;TestTemplate
 *         &#064;VaryFeatures(MyVariationSetProvider.class)
 *         void run() {
 *             // run testing code ...
 *             // ONE is enabled
 *             // TWO is disabled
 *             // THREE is disabled in the first run and enabled in the second run
 *         }
 *
 *         private static class MyVariationSetProvider implements VariationSetProvider {
 *
 *             &#064;Override
 *             public VariationSet<? extends Feature> buildVariationSet() {
 *                 return create(MyFeatures.class).enable(MyFeatures.ONE).disable(MyFeatures.TWO).vary(MyFeatures.THREE);
 *             }
 *         }
 *     }
 * </pre>
 *
 * @author Roland Weisleder
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@ExtendWith(FeatureVariationTogglzExtension.class)
public @interface VaryFeatures {

    Class<? extends VariationSetProvider> value();

}
