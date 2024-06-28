package org.togglz.test;

import org.togglz.core.manager.FeatureManager;
import org.togglz.core.spi.FeatureManagerProvider;

/**
 *
 * This {@link FeatureManagerProvider} is used by the testing integration modules to provide a FeatureManager in unit
 * tests.
 */
public class TestFeatureManagerProvider implements FeatureManagerProvider {

  private final FeatureManager featureManager;

  /**
   * The constructor.
   */
  public TestFeatureManagerProvider() {

    super();
    this.featureManager = new TestFeatureManager();
  }

  @Override
  public int priority() {

    // very high priority
    return 100;
  }

  @Override
  public FeatureManager getFeatureManager() {

    return this.featureManager;
  }

}
