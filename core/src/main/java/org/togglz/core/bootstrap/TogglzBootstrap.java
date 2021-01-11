package org.togglz.core.bootstrap;

import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.manager.TogglzConfig;

/**
 * <p>
 * Implementations of this interface are looked up by Togglz during the bootstrapping process and used to create the
 * FeatureManager for the application. The automatic bootstrapping is the default behavior for web applications.
 * </p>
 *
 * <p>
 * Users can choose whether they want to implement {@link TogglzBootstrap} or {@link TogglzConfig} for configuring Togglz. The
 * advantage of {@link TogglzBootstrap} is that it offers more control than {@link TogglzConfig} while {@link TogglzConfig} is
 * easier to use but doesn't allow to configure all the aspects of the {@link FeatureManager}.
 * </p>
 *
 * @see FeatureManagerBootstrapper
 * @see TogglzConfig
 *
 * @author Christian Kaltepoth
 *
 */
public interface TogglzBootstrap {

    /**
     * Create the {@link FeatureManager} for the application. Implementations typically use {@link FeatureManagerBuilder} which
     * offers a fluent API for configuring all the aspects of Togglz.
     *
     * @return The new {@link FeatureManager}, never {@code null}.
     */
    FeatureManager createFeatureManager();

}
