package org.togglz.shiro;

import org.apache.shiro.subject.Subject;
import org.togglz.core.user.FeatureUser;

/**
 * 
 * Implementation of {@link FeatureUser} representing a {@link Subject} of Apache Shiro.
 * 
 * @author Christian Kaltepoth
 * 
 */
public class ShiroFeatureUser implements FeatureUser {

    private final Subject subject;

    private final String featureAdminRole;

    /**
     * Creates a new {@link ShiroFeatureUser}. This constructor requires the caller to supply an authenticated {@link Subject}
     * and the role identifier that is used to check whether the user is a feature admin.
     * 
     * @param subject The {@link Subject} of the current user
     * @param featureAdminRole The name of the feature admin role
     */
    public ShiroFeatureUser(Subject subject, String featureAdminRole) {
        this.subject = subject;
        this.featureAdminRole = featureAdminRole;
    }

    @Override
    public String getName() {
        return subject.getPrincipal().toString();
    }

    @Override
    public boolean isFeatureAdmin() {
        return subject.hasRole(featureAdminRole);
    }

}
