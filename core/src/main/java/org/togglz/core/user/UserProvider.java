package org.togglz.core.user;

/**
 * 
 * Implementations of this class a responsible to identify the current user acting in the application.
 * 
 * @author Christian Kaltepoth
 * 
 */
public interface UserProvider {

    /**
     * Return a {@link FeatureUser} instance representing the current user. This method should return <code>null</code> if the
     * implementation is unable to determine the user.
     * 
     * @return current user or <code>null</code>
     */
    FeatureUser getCurrentUser();

}
