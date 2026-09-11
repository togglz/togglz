package org.togglz.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.togglz.core.context.FeatureContext;

class FeatureEnumResitrationTest {

  @BeforeAll
  static void setup() {

    TestFeatureManagerProvider.register(MyFeatures.class);
  }

  @Test
  void testFeaturesRegisteredWithoutAskingForActivity() {

    assertThat(FeatureContext.getFeatureManager().getFeatures())
        .containsExactlyInAnyOrder(MyFeatures.class.getEnumConstants());
  }

}
