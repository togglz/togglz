package org.togglz.core.manager;

import org.togglz.core.Feature;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.user.UserProvider;
import org.togglz.core.user.NoOpUserProvider;

/**
 * 
 * This interface represents the configuration of Togglz. It must be implemented by the user to tell Togglz about the feature
 * enum, how feature state is persisted and which way Togglz should use to obtain the current user.
 * 
 * @author Christian Kaltepoth
 * 
 */
public interface TogglzConfig {

    /**
     * Used to tell Togglz about the feature enum that you want to use. Please note that your feature enum has to implement the
     * {@link Feature} interface.
     * 
     * @return The feature enum, never <code>null</code>
     */
    Class<? extends Feature> getFeatureClass();

    /**
     * <p>
     * The {@link StateRepository} Togglz should use to store feature state. Please refer to the Togglz documentation of
     * a list of default implementations that ship with Togglz.
     * </p>
     * 
     * <p>
     * Please note that this method is only called once. So you can safely implement the method by returning a new instance of
     * an anonymous class.
     * </p>
     * 
     * @return The repository, never <code>null</code>
     */
    StateRepository getStateRepository();

    /**
     * <p>
     * The {@link UserProvider} Togglz should use to obtain the current user. Please refer to the Togglz documentation of
     * a list of default implementations that ship with Togglz. If you don't want to be able to toggle feature on a per user
     * basis and are not planning to use the Togglz Console, you can return {@link NoOpUserProvider} here.
     * </p>
     * 
     * <p>
     * Please note that this method is only called once. So you can safely implement the method by returning a new instance of
     * an anonymous class.
     * </p>
     * 
     * @return The feature user provider, never <code>null</code>
     */
    UserProvider getUserProvider();

}
