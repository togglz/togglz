package org.togglz.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.repository.FeatureState;

class DefaultFeatureStateTest {

  @Test
  void testFeaturesActiveByDefault() {

    assertTrue(MyFeatures.FEATURE_ONE.isActive());
    assertFalse(MyFeatures.FEATURE_TWO.isActive());
  }

  @Test
  void testFeatureDynamic() {

    assertTrue(MyFeatures.FEATURE_ONE.isActive());
    FeatureContext.getFeatureManager().setFeatureState(new FeatureState(MyFeatures.FEATURE_ONE, false));
    assertFalse(MyFeatures.FEATURE_ONE.isActive());
  }

}
