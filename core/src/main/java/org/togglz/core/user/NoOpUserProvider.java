package org.togglz.core.user;

/**
 * 
 * This implementation of {@link UserProvider} can be used if user-dependent feature toggling isn't used at all.
 * 
 * @author Christian Kaltepoth
 * 
 */
public class NoOpUserProvider implements UserProvider {

    @Override
    public FeatureUser getCurrentUser() {
        return null;
    }

}
