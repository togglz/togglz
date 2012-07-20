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

/**
 * TODO
 * 
 * @author Christian Kaltepoth
 */
public class FeatureVariations extends Suite {

    protected List<Runner> runners = new ArrayList<Runner>();

    public FeatureVariations(Class<?> clazz) throws InitializationError {
        super(clazz, Collections.<Runner> emptyList());

        TestClass testClass = new TestClass(clazz);

        VariationSetBuilder<? extends Feature> permutation = getPermutationFromMethod(testClass);
        if (permutation == null) {
            throw new IllegalStateException("You have to place a @" + Variations.class.getSimpleName()
                    + " annotation one the class: " + clazz.getName());
        }

        for (Set<? extends Feature> activeFeatures : permutation.getVariants()) {
            runners.add(new VariationRunner(clazz, permutation.getFeatureClass(), activeFeatures));
        }

    }

    private VariationSetBuilder<? extends Feature> getPermutationFromMethod(TestClass testClass) {

        List<FrameworkMethod> methods = testClass.getAnnotatedMethods(Variations.class);
        for (FrameworkMethod method : methods) {
            int modifiers = method.getMethod().getModifiers();
            if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
                try {
                    return (VariationSetBuilder) method.invokeExplosively(null);
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
