package org.togglz.core.user;

/**
 * 
 * This interface represents a user for Togglz
 * 
 * @author Christian Kaltepoth
 * 
 */
public interface FeatureUser {

    /**
     * Get the unique name for this use
     * 
     * @return name of the user
     */
    String getName();

    /**
     * Is the user a feature admin, which means that he is able to use the Togglz Admin Console.
     * 
     * @return <code>true</code> for feature admins, <code>false</code> otherwise.
     */
    boolean isFeatureAdmin();

    /**
     * This method allows to retrieve attributes associated with a user.
     * 
     * @param name The name of the attribute
     * @return the value of the attribute or <code>null</code> if there is no such attribute.
     */
    Object getAttribute(String name);

}
