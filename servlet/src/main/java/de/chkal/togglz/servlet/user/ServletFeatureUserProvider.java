package de.chkal.togglz.servlet.user;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import de.chkal.togglz.core.user.FeatureUser;
import de.chkal.togglz.core.user.FeatureUserProvider;
import de.chkal.togglz.core.user.SimpleFeatureUser;
import de.chkal.togglz.servlet.util.HttpServletRequestHolder;

public class ServletFeatureUserProvider implements FeatureUserProvider {

    public final static String DEFAULT_FEATURE_ADMIN_ROLE = "togglz";

    private final String featureAdminRole;

    public ServletFeatureUserProvider() {
        this(DEFAULT_FEATURE_ADMIN_ROLE);
    }

    public ServletFeatureUserProvider(String featureAdminRole) {
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
