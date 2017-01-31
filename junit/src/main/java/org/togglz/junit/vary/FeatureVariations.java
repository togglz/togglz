package org.togglz.junit.vary;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.testing.TestFeatureManager;
import org.togglz.testing.TestFeatureManagerProvider;

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
public class FeatureVariations extends BlockJUnit4ClassRunner {

    public FeatureVariations(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected void collectInitializationErrors(List<Throwable> errors) {
        super.collectInitializationErrors(errors);

        validateVariationsFields(errors);
    }

    private void validateVariationsFields(List<Throwable> errors) {
        List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(Variations.class);

        for (FrameworkMethod method : methods) {
            int modifiers = method.getMethod().getModifiers();

            if (!Modifier.isPublic(modifiers)) {
                errors.add(new Error("Variations method " + method.getName() + " must be public"));
            }
            if (!Modifier.isStatic(modifiers)) {
                errors.add(new Error("Variations method " + method.getName() + " must be static"));
            }
        }

        if (methods.size() != 1) {
            errors.add(new Error("@" + Variations.class.getSimpleName() + " annotation must be present on a single"
                + "method in the class: " + getTestClass().getName()));
        }
    }

    @Override
    protected Statement methodBlock(FrameworkMethod method) {
        return new VariationAnchor(method);
    }

    public class VariationAnchor extends Statement {

        private final FrameworkMethod method;

        public VariationAnchor(FrameworkMethod method) {
            this.method = method;
        }

        @Override
        public void evaluate() throws Throwable {
            TestClass testClass = getTestClass();
            FrameworkMethod permutationMethod = testClass.getAnnotatedMethods(Variations.class).get(0);
            VariationSetBuilder<? extends Feature> permutation;

            try {
                permutation = (VariationSetBuilder) permutationMethod.invokeExplosively(null);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }

            for (Set<? extends Feature> activeFeatures : permutation.getVariants()) {
                evaluateInternal(permutation.getFeatureClass(), activeFeatures);
            }
        }

        private void evaluateInternal(Class<? extends Feature> featureClass, Set<? extends Feature> activeFeatures)
            throws Throwable {
            try {
                TestFeatureManager featureManager = new TestFeatureManager(featureClass);
                for (Feature feature : activeFeatures) {
                    featureManager.enable(feature);
                }

                TestFeatureManagerProvider.setFeatureManager(featureManager);
                FeatureContext.clearCache();

                FeatureVariations.super.methodBlock(method).evaluate();
            } finally {
                TestFeatureManagerProvider.setFeatureManager(null);
            }
        }
    }
}
