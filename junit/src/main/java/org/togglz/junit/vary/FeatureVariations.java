package org.togglz.junit.vary;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;
import org.togglz.core.Feature;
import org.togglz.testing.vary.VariationSet;

/**
 * <p>
 * This class is custom JUnit runner that allows to run tests against different feature combinations. A usecase for this runner
 * would be for example to test feature toggles that enable caching behavior. The runner can be configured to run the tests two
 * times. Once with the flag enabled and once with the flag being disabled.
 * </p>
 * 
 * <p>
 * Tests executed with this runner must implement a public static method annotated with {@link Variations}. This method must
 * return a {@link VariationSet} set specifies which features should be varied.
 * </p>
 * 
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>
 * &#064;RunWith(FeatureVariations.class)
 * public class FeatureVariationsTest {
 * 
 *     &#064;Variations
 *     public static VariationSet&lt;MyFeatures&gt; getPermutations() {
 *         return VariationSetBuilder.create(MyFeatures.class)
 *                 .enable(MyFeatures.FEATURE_ONE)
 *                 .vary(MyFeatures.FEATURE_TWO);
 *     }
 * 
 *     &#064;Test
 *     public void test() {
 *         // do the tests
 *     }
 * 
 * }
 * </pre>
 * 
 * <p>
 * In this example the test is executed two times. Once with <code>FEATURE_TWO</code> being active and once with
 * <code>FEATURE_TWO</code> being inactive. In both runs <code>FEATURE_ONE</code> is enabled.
 * 
 * @author Christian Kaltepoth
 */
public class FeatureVariations extends Suite {

    protected List<Runner> runners = new ArrayList<Runner>();

    public FeatureVariations(Class<?> clazz) throws InitializationError {
        super(null, Collections.<Runner> emptyList());

        TestClass testClass = new TestClass(clazz);

        VariationSet<? extends Feature> permutation = getPermutationFromMethod(testClass);
        if (permutation == null) {
            throw new IllegalStateException("You have to place a @" + Variations.class.getSimpleName()
                    + " annotation one the class: " + clazz.getName());
        }

        for (Set<? extends Feature> activeFeatures : permutation.getVariants()) {
            runners.add(new VariationRunner(clazz, permutation.getFeatureClass(), activeFeatures));
        }

    }

    private VariationSet<? extends Feature> getPermutationFromMethod(TestClass testClass) {

        List<FrameworkMethod> methods = testClass.getAnnotatedMethods(Variations.class);
        for (FrameworkMethod method : methods) {
            int modifiers = method.getMethod().getModifiers();
            if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
                try {
                    return (VariationSet) method.invokeExplosively(null);
                } catch (Throwable e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        throw new IllegalStateException("Could not find public static method annotated with @"
                + Variations.class.getSimpleName() + " on class: " + testClass.getName());

    }

    @Override
    protected List<Runner> getChildren() {
        return runners;
    }

}
