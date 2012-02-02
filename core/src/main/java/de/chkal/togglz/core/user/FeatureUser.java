package de.chkal.togglz.core.user;

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

}
