package org.togglz.test.user.seam.security;

import org.jboss.seam.security.Identity;
import org.jboss.seam.security.annotations.Secures;
import org.togglz.seam.security.FeatureAdmin;


public class SeamSecurityAuthorizer {

    @Secures
    @FeatureAdmin
    public boolean isFeatureAdmin(Identity identity) {
        return identity.getUser().getId().equals("ck");
    }
}
