package org.togglz.junit.vary;

import java.util.Set;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.testing.TestFeatureManager;
import org.togglz.testing.TestFeatureManagerProvider;

/**
 * Internal class that executes a test for a test class for a single feature variation.
 * 
 * @author Christian Kaltepoth
 */
class VariationRunner extends BlockJUnit4ClassRunner {

    private final Class<? extends Feature> featureClass;

    private final Set<? extends Feature> activeFeatures;

    public VariationRunner(Class<?> testClass, Class<? extends Feature> featureClass,
            Set<? extends Feature> activeFeatures) throws InitializationError {
        super(testClass);
        this.featureClass = featureClass;
        this.activeFeatures = activeFeatures;
    }

    @Override
    protected Statement methodInvoker(FrameworkMethod method, Object test) {

        final Statement delegate = super.methodInvoker(method, test);

        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                try {

                    // create blank instance and set initial state
                    TestFeatureManager featureManager = new TestFeatureManager(featureClass);
                    for (Feature feature : activeFeatures) {
                        featureManager.enable(feature);
                    }

                    // register the test instance
                    TestFeatureManagerProvider.setFeatureManager(featureManager);
                    FeatureContext.clearCache();

                    // run the test
                    delegate.evaluate();

                }

                finally {
                    TestFeatureManagerProvider.setFeatureManager(null);
                }
            }
        };
    }

}
