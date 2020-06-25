package org.togglz.testing;

import org.junit.jupiter.api.Test;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.repository.FeatureState;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultFeatureStateTest {

    @Test
    void testFeaturesActiveByDefault() {
        assertTrue(MyFeatures.FEATURE_ONE.isActive());
    }

    @Test
    void testFeatureManagerImmutable() {
        assertThrows(UnsupportedOperationException.class, () -> {
            FeatureContext.getFeatureManager().setFeatureState(new FeatureState(MyFeatures.FEATURE_ONE, false));
        });
    }

}
