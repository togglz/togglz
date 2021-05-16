package org.togglz.core.proxy;

import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;

/**
 * Simple switch which sets its delegate to one of two objects depending on the state of the specified {@link Feature}.
 * @see ByteBuddyProxyFactory ByteBuddyProxyFactory uses this as the baseclass for generated proxies
 */
public abstract class TogglzSwitchable<T> {
  private final FeatureManager featureManager;
  private final Feature feature;
  private final T active;
  private final T inactive;
  protected T delegate;

  public TogglzSwitchable(FeatureManager featureManager, Feature feature, T active, T inactive) {
    this.featureManager = featureManager;
    this.feature = feature;
    this.active = active;
    this.inactive = inactive;

    delegate = featureManager.isActive(feature) ? active : inactive;
  }

  /**
   * Updates the internal delegate selection against the {@link Feature} state.
   */
  protected final void checkTogglzState() {
    boolean configured = featureManager.isActive(feature);
    boolean operational = delegate == active;
    if (configured ^ operational) { // Less field writes -> more JIT optimisations
      delegate = configured ? active : inactive;
    }
  }

  /**
   * Manually update the internal delegation of a {@link TogglzSwitchable} against its
   * {@link Feature} state. This is intended for use with passive switching.
   * @param o The object to update. If null or not a {@link TogglzSwitchable}, no action is taken.
   */
  public static void update(Object o) {
    if ( o instanceof TogglzSwitchable) {
      ((TogglzSwitchable<?>)o).checkTogglzState();
    }
  }
}
