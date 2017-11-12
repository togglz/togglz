package org.togglz.junit5;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.togglz.testing.vary.VariationSetBuilder.create;

import org.junit.jupiter.api.TestTemplate;
import org.togglz.core.Feature;
import org.togglz.testing.vary.VariationSet;

/**
 * @author Roland Weisleder
 */
class FeatureVariationTogglzExtensionTest {

    @TestTemplate
    @VaryFeatures(TestingVariationSetProvider.class)
    void variations() {
        assertTrue(MyFeatures.ONE.isActive());
        assertFalse(MyFeatures.TWO.isActive());

        // active or not active, that is the question
        assertTrue(MyFeatures.THREE.isActive() || !MyFeatures.THREE.isActive());
    }

    private static class TestingVariationSetProvider implements VariationSetProvider {

        @Override
        public VariationSet<? extends Feature> buildVariationSet() {
            return create(MyFeatures.class).enable(MyFeatures.ONE).disable(MyFeatures.TWO).vary(MyFeatures.THREE);
        }
    }

}
