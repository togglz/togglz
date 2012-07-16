package org.togglz.testing;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.repository.FeatureState;

public class DefaultFeatureStateTest {

    @Test
    public void testFeaturesActiveByDefault() {
        assertTrue(MyFeatures.FEATURE_ONE.isActive());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testFeatureManagerImmutable() {
        FeatureContext.getFeatureManager().setFeatureState(new FeatureState(MyFeatures.FEATURE_ONE, false));
    }

}
