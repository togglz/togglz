package org.togglz.test;

import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.spi.FeatureManagerProvider;

/**
 *
 * This {@link FeatureManagerProvider} is used by the testing integration modules to provide a FeatureManager in unit
 * tests.
 */
public class TestFeatureManagerProvider implements FeatureManagerProvider {

  private static final TestFeatureManager FEATURE_MANAGER = new TestFeatureManager();

  /**
   * The constructor.
   */
  public TestFeatureManagerProvider() {

    super();
  }

  @Override
  public int priority() {

    // very high priority
    return 100;
  }

  @Override
  public FeatureManager getFeatureManager() {

    return FEATURE_MANAGER;
  }

  /**
   * @param featureEnum the {@link Class} reflecting the {@link Feature} {@link Enum}.
   */
  public static void register(Class<? extends Feature> featureEnum) {

    FEATURE_MANAGER.addFeatureEnum(featureEnum);
  }

}
