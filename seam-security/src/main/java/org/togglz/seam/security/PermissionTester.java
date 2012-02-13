package org.togglz.seam.security;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PermissionTester {

    @FeatureAdmin
    public void testFeatureAdminPermission() {
        // do nothing
    }

}
