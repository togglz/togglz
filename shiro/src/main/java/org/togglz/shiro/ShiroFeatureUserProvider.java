package org.togglz.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.FeatureUserProvider;
import org.togglz.core.user.SimpleFeatureUser;

/**
 * 
 * A {@link FeatureUserProvider} implementation for Apache Shiro.
 * 
 * @author Christian Kaltepoth
 * 
 */
public class ShiroFeatureUserProvider implements FeatureUserProvider {

    private String featureAdminRole;

    /**
     * Constructor for the provider.
     * 
     * @param featureAdminRole The role identifier to check whether the users are feature admins.
     */
    public ShiroFeatureUserProvider(String featureAdminRole) {
        this.featureAdminRole = featureAdminRole;
    }

    @Override
    public FeatureUser getCurrentUser() {

        // will always return a result
        Subject subject = SecurityUtils.getSubject();

        // only allow authenticated user
        if (subject.isAuthenticated()) {
            return new SimpleFeatureUser(subject.getPrincipal().toString(), subject.hasRole(featureAdminRole));
        }

        // user is not authenticated
        return null;

    }
}
