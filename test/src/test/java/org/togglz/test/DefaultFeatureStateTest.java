package org.togglz.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.togglz.core.context.FeatureContext;

class DefaultFeatureStateTest {

  @Test
  void testFeaturesActiveByDefault() {

    assertTrue(MyFeatures.FEATURE_ONE.isActive());
    assertFalse(MyFeatures.FEATURE_TWO.isActive());
  }

  @Test
  void testFeatureDynamic() {

    assertTrue(MyFeatures.FEATURE_ONE.isActive());
    FeatureContext.getFeatureManager().disable(MyFeatures.FEATURE_ONE);
    assertFalse(MyFeatures.FEATURE_ONE.isActive());
    FeatureContext.getFeatureManager().enable(MyFeatures.FEATURE_ONE);
    assertTrue(MyFeatures.FEATURE_ONE.isActive());
  }

}
