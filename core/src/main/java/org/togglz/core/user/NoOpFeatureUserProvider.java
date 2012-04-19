package org.togglz.core.user;

/**
 * 
 * This implementation of {@link FeatureUserProvider} can be used if user-dependent feature toggling isn't used at all.
 * 
 * @author Christian Kaltepoth
 * 
 */
public class NoOpFeatureUserProvider implements FeatureUserProvider {

    @Override
    public FeatureUser getCurrentUser() {
        return null;
    }

}
