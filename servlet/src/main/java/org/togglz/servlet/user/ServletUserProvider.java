package org.togglz.servlet.user;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.UserProvider;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.servlet.util.HttpServletRequestHolder;

/**
 * 
 * Implementation of {@link UserProvider} that uses {@link HttpServletRequest#getUserPrincipal()} to obtain the user.
 * 
 * @author Christian Kaltepoth
 * 
 */
public class ServletUserProvider implements UserProvider {

    private final String featureAdminRole;

    /**
     * This constructor requires you to supply the name of the role that identifies users to be feature admins.
     * 
     * @param featureAdminRole the feature admin role name
     */
    public ServletUserProvider(String featureAdminRole) {
        this.featureAdminRole = featureAdminRole;
    }

    @Override
    public FeatureUser getCurrentUser() {

        HttpServletRequest request = HttpServletRequestHolder.get();

        if (request == null) {
            throw new IllegalStateException(
                    "Could not get request from HttpServletRequestHolder. Did you configure the TogglzFilter correctly?");
        }

        Principal principal = request.getUserPrincipal();

        if (principal != null) {

            boolean featureAdmin = request.isUserInRole(featureAdminRole);

            return new SimpleFeatureUser(principal.getName(), featureAdmin);

        }

        return null;

    }
}
