package org.togglz.junit5;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.togglz.core.context.FeatureContext;
import org.togglz.testing.TestFeatureManager;
import org.togglz.testing.TestFeatureManagerProvider;

/**
 * <p>
 *     JUnit Extension, which sets up a {@link TestFeatureManager} and provides it as parameter to test methods.
 * </p>
 *
 * <p>
 *     Implementation classes must provide a fully initialized {@link TestFeatureManager}.
 * </p>
 *
 * @see AnnotationBasedTogglzExtension
 * @see FeatureVariationInvocationContext
 *
 * @author Roland Weisleder
 */
abstract class FeatureManagerExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private TestFeatureManager featureManager = null;

    abstract TestFeatureManager createTestFeatureManager(ExtensionContext context);

    @Override
    public void beforeEach(ExtensionContext context) {
        featureManager = createTestFeatureManager(context);

        TestFeatureManagerProvider.setFeatureManager(featureManager);
        FeatureContext.clearCache();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        TestFeatureManagerProvider.setFeatureManager(null);
        FeatureContext.clearCache();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType().isAssignableFrom(TestFeatureManager.class);
    }

    @Override
    public TestFeatureManager resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return featureManager;
    }

}
