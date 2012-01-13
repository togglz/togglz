package de.chkal.togglz.seam.security;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.seam.security.AuthorizationException;
import org.jboss.seam.security.Identity;

import de.chkal.togglz.core.user.FeatureUser;
import de.chkal.togglz.core.user.FeatureUserProvider;
import de.chkal.togglz.core.user.SimpleFeatureUser;

@ApplicationScoped
public class SeamSecurityFeatureUserProvider implements FeatureUserProvider {

    @Inject
    private Identity identity;

    @Inject
    private PermissionTester permissionTester;
    
    @Override
    public FeatureUser getCurrentUser() {

        if (identity != null) {
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
