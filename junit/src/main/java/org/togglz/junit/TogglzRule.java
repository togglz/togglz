package org.togglz.junit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.testing.TestFeatureManager;
import org.togglz.testing.TestFeatureManagerProvider;

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

    private TogglzRule(Class<? extends Feature> featureClass, Feature[] initiallyActive) {
        this.featureClass = featureClass;
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
                    for(Feature feature : initiallyActive) {
                        featureManager.enable(feature);
                    }

                    // register the test instance
                    TestFeatureManagerProvider.setFeatureManager(featureManager);
                    FeatureContext.clearCache();

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
