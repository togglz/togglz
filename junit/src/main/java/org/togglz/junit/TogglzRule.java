package org.togglz.junit;

import java.util.Set;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.util.NamedFeature;
import org.togglz.core.util.Validate;
import org.togglz.testing.TestFeatureManager;
import org.togglz.testing.TestFeatureManagerProvider;
import org.togglz.testing.vary.VariationSetBuilder;

/**
 * <p>
 * JUnit rule that simplifies the process of controlling features in unit tests.
 * </p>
 * <p>
 * Example usage:
 * </p>
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
 */
public class TogglzRule implements TestRule {

    private static final Logger log = LoggerFactory.getLogger(TogglzRule.class);

    private final Class<? extends Feature> featureClass;

    private final Set<Set<Feature>> variants;

    private TestFeatureManager featureManager;

    public static Builder builder(final Class<? extends Feature> featuresClass) {
        return new Builder(featuresClass);
    }

    public static TogglzRule allEnabled(Class<? extends Feature> featureClass) {
        return builder(featureClass).enableAll().build();
    }

    public static TogglzRule allDisabled(Class<? extends Feature> featureClass) {
        return builder(featureClass).disableAll().build();
    }

    private TogglzRule(Class<? extends Feature> featureEnum, Set<Set<Feature>> variants) {
        Validate.notNull(featureEnum, "The featureEnum argument is required");
        Validate.isTrue(featureEnum.isEnum(), "This class only works with feature enums");
        this.featureClass = featureEnum;
        this.variants = variants;
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

                    WithFeature withFeature = description.getAnnotation(WithFeature.class);

                    for (Set<Feature> activeFeatures : variants) {

                        // create blank instance and set initial state
                        featureManager = new TestFeatureManager(featureClass);
                        for (Feature feature : activeFeatures) {
                            featureManager.enable(feature);
                        }

                        // register the test instance
                        TestFeatureManagerProvider.setFeatureManager(featureManager);
                        FeatureContext.clearCache();

                        // apply @WithFeature annotation
                        if (withFeature != null) {

                            // @WithFeature is only supported if there are no variations
                            if (variants.size() == 1) {
                                for (String featureName : withFeature.value()) {
                                    Feature feature = new NamedFeature(featureName);
                                    if (withFeature.disable()) {
                                        disable(feature);
                                    } else {
                                        enable(feature);
                                    }
                                }
                            }

                            // warn the user
                            else {
                                log.info("Ignoring @ViewFeature because the rule defines feature variations");
                            }

                        }

                        // run the test
                        base.evaluate();

                    }

                } finally {
                    featureManager = null;
                    TestFeatureManagerProvider.setFeatureManager(null);
                    FeatureContext.clearCache();
                }

            }
        };

    }

    public void disable(Feature feature) {
        featureManager.disable(feature);
    }

    public void enable(Feature feature) {
        featureManager.enable(feature);
    }

    public static class Builder {

        private final Class<? extends Feature> featuresClass;
        private final VariationSetBuilder<Feature> variationSetBuilder;

        public Builder(final Class<? extends Feature> featuresClass) {
            this.featuresClass = featuresClass;
            this.variationSetBuilder = VariationSetBuilder.create((Class<Feature>) featuresClass);
        }

        public Builder enable(final Feature f) {
            variationSetBuilder.enable(f);
            return this;
        }

        public Builder enableAll() {
            variationSetBuilder.enableAll();
            return this;
        }

        public Builder disable(final Feature f) {
            variationSetBuilder.disable(f);
            return this;
        }

        public Builder disableAll() {
            variationSetBuilder.disableAll();
            return this;
        }

        public Builder vary(final Feature f) {
            variationSetBuilder.vary(f);
            return this;
        }

        public TogglzRule build() {
            return new TogglzRule(featuresClass, variationSetBuilder.getVariants());
        }

    }

}
