package org.togglz.core.user.jaas;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Principal;
import java.util.Iterator;

import javax.security.auth.Subject;

import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;

/**
 * 
 * This implementation supports looking up the current user the JAAS AccessControlContext. The class currently doesn't support
 * determining whether a user is a feature admin and therefore always sets it to false. Overriding
 * {@link #isFeatureAdmin(String)} allows to change the default behavior.
 * 
 * @author Christian Kaltepoth
 * 
 */
public class JAASUserProvider implements UserProvider {

    @Override
    public FeatureUser getCurrentUser() {

        AccessControlContext acc = AccessController.getContext();
        if (acc != null) {
            Subject subject = Subject.getSubject(acc);
            if (subject != null) {
                Iterator<Principal> iter = subject.getPrincipals().iterator();
                if (iter.hasNext()) {
                    Principal principal = iter.next();
                    return new SimpleFeatureUser(principal.getName(), isFeatureAdmin(principal));
                }
            }
        }
        return null;
    }

    /**
     * Checks if the supplied user is a feature admin. The default implementation always returns <code>false</code>. Users can
     * overwrite this method to implement a different behavior.
     * 
     * @param principal The principal
     * @return <code>true</code> for feature admins, <code>false</code> otherwise
     */
    protected boolean isFeatureAdmin(Principal principal) {
        return false;
    }

}
