package org.togglz.junit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;

public class TogglzRule implements TestRule {

    private final Class<? extends Feature> featureClass;

    private TestFeatureManager featureManager;

    public TogglzRule(Class<? extends Feature> featureClass) {
        this.featureClass = featureClass;
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

                    featureManager = new TestFeatureManager(featureClass);
                    TestFeatureManagerProvider.setFeatureManager(featureManager);
                    FeatureContext.clearCache();

                    base.evaluate();
                    
                }

                finally {
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

}
