package org.togglz.deltaspike;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.security.api.Identity;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;

/**
 * 
 * Initial integration for Apache DeltaSpike. Please not that this implementation currently doesn't support the feature admin
 * flag and therefore always sets it to <code>false</code>. Overriding {@link #isFeatureAdmin(Identity)} allows to change the
 * default behavior.
 * 
 * @author Christian Kaltepoth
 * 
 */
public class DeltaSpikeUserProvider implements UserProvider {

    @Override
    public FeatureUser getCurrentUser() {

        Identity identity = BeanProvider.getContextualReference(Identity.class);
        if (identity == null) {
            throw new IllegalStateException("Could not obtain Identity");
        }

        if (identity.isLoggedIn()) {
            String name = identity.getUser().getId();
            return new SimpleFeatureUser(name, isFeatureAdmin(identity));
        }

        return null;
    }

    /**
     * Checks if the supplied user is a feature admin. The default implementation always returns <code>false</code>. Users can
     * overwrite this method to implement a different behavior.
     * 
     * @param identity the identity of the user
     * @return <code>true</code> for feature admins, <code>false</code> otherwise
     */
    protected boolean isFeatureAdmin(Identity identity) {
        return false;
    }

}
