package org.togglz.junit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.util.NamedFeature;
import org.togglz.core.util.Validate;
import org.togglz.testing.TestFeatureManager;
import org.togglz.testing.TestFeatureManagerProvider;

/**
 * <p>
 * JUnit rule that simplifies the process of controlling features in unit tests.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>
 * public class TogglzRuleAllEnabledTest {
 * 
 *   &#064;Rule
 *   public TogglzRule togglzRule = TogglzRule.allEnabled(MyFeatures.class);
 * 
 *   &#064;Test
 *   public void testToggleFeature() {
 * 
 *     assertTrue(MyFeatures.FEATURE_ONE.isActive());
 * 
 *     togglzRule.disable(MyFeatures.FEATURE_ONE);
 *      
 *     assertFalse(MyFeatures.FEATURE_ONE.isActive());
 *     
 *   }
 * 
 * }
 * </pre>
 * 
 * @author Christian Kaltepoth
 * 
 */
public class TogglzRule implements TestRule {

    private final Class<? extends Feature> featureClass;

    private Feature[] initiallyActive;

    private TestFeatureManager featureManager;

    public static TogglzRule allEnabled(Class<? extends Feature> featureClass) {
        return new TogglzRule(featureClass, featureClass.getEnumConstants());
    }

    public static TogglzRule allDisabled(Class<? extends Feature> featureClass) {
        return new TogglzRule(featureClass, new Feature[0]);
    }

    private TogglzRule(Class<? extends Feature> featureEnum, Feature[] initiallyActive) {
        Validate.notNull(featureEnum, "The featureEnum argument is required");
        Validate.isTrue(featureEnum.isEnum(), "This class only works with feature enums");
        this.featureClass = featureEnum;
        this.initiallyActive = initiallyActive;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {

        return new Statement() {

            @Override
            public void evaluate() throws Throwable {

                try {

                    if (featureManager != null) {
                        throw new IllegalStateException("Found existing TestFeatureManager");
                    }

                    // create blank instance and set initial state
                    featureManager = new TestFeatureManager(featureClass);
                    for (Feature feature : initiallyActive) {
                        featureManager.enable(feature);
                    }

                    // register the test instance
                    TestFeatureManagerProvider.setFeatureManager(featureManager);
                    FeatureContext.clearCache();

                    WithFeature withFeature = description.getAnnotation(WithFeature.class);
                    if (withFeature != null) {
                        for (String featureName : withFeature.value()) {
                            if (withFeature.disable()) {
                                disable(new NamedFeature(featureName));
                            } else {
                                enable(new NamedFeature(featureName));
                            }
                        }
                    }
                    
                    // run the test
                    base.evaluate();

                }

                finally {
                    featureManager = null;
                    TestFeatureManagerProvider.setFeatureManager(null);
                }

            }
        };

    }

    public TestFeatureManager getFeatureManager() {
        return featureManager;
    }

    public void disable(Feature feature) {
        featureManager.disable(feature);
    }

    public void enable(Feature feature) {
        featureManager.enable(feature);
    }

    public void enableAll() {
        featureManager.enableAll();
    }

    public void disableAll() {
        featureManager.disableAll();
    }

}
