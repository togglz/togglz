package org.togglz.seam.security;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.seam.security.AuthorizationException;
import org.jboss.seam.security.Identity;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.UserProvider;
import org.togglz.core.user.SimpleFeatureUser;


@ApplicationScoped
public class SeamSecurityUserProvider implements UserProvider {

    @Inject
    private Identity identity;

    @Inject
    private PermissionTester permissionTester;

    @Override
    public FeatureUser getCurrentUser() {

        if (identity != null && identity.getUser() != null) {
            return new SimpleFeatureUser(identity.getUser().getId(), isFeatureAdmin());
        }

        return null;

    }

    private boolean isFeatureAdmin() {
        try {
            permissionTester.testFeatureAdminPermission();
            return true;
        } catch (AuthorizationException e) {
            return false;
        }
    }
}
