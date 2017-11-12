package org.togglz.junit5;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.togglz.core.Feature;
import org.togglz.testing.TestFeatureManager;

/**
 * <p>
 *     Represents a single {@link TestTemplateInvocationContext} created by the {@link FeatureVariationTogglzExtension}
 *     which contains a set of enabled features.
 * </p>
 *
 * <p>
 *     This invocation contexts adds a {@link FeatureManagerExtension} to the current test.
 * </p>
 *
 * <p>
 *     Before executing the test it reports the set of enabled features so others can understand which invocation
 *     belongs to which feature variation.
 * </p>
 *
 * @see FeatureVariationTogglzExtension
 * @see FeatureManagerExtension
 *
 * @author Roland Weisleder
 */
class FeatureVariationInvocationContext implements TestTemplateInvocationContext {

    private final Class<? extends Feature> featureClass;

    private final Set<? extends Feature> enabledFeatures;

    FeatureVariationInvocationContext(Class<? extends Feature> featureClass, Set<? extends Feature> enabledFeatures) {
        this.featureClass = featureClass;
        this.enabledFeatures = enabledFeatures;
    }

    @Override
    public List<Extension> getAdditionalExtensions() {
        return singletonList(new FeatureVariantExtension());
    }

    private class FeatureVariantExtension extends FeatureManagerExtension {

        @Override
        TestFeatureManager createTestFeatureManager(ExtensionContext context) {
            TestFeatureManager featureManager = new TestFeatureManager(featureClass);
            featureManager.disableAll();
            enabledFeatures.forEach(featureManager::enable);
            return featureManager;
        }

        @Override
        public void beforeEach(ExtensionContext context) {
            String names = enabledFeatures.stream().map(Feature::name).sorted().collect(joining(", ", "[", "]"));
            context.publishReportEntry("enabledFeatures", names);

            super.beforeEach(context);
        }
    }
}
