package org.togglz.spring.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.FeatureUserProvider;
import org.togglz.core.user.SimpleFeatureUser;


public class SpringSecurityFeatureUserProvider implements FeatureUserProvider {

    private final String featureAdminAuthority;

    public SpringSecurityFeatureUserProvider(String featureAdminAuthority) {
        this.featureAdminAuthority = featureAdminAuthority;
    }

    @Override
    public FeatureUser getCurrentUser() {

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        // null if no authentication data is available for the current thread
        if (authentication != null) {

            String name = null;

            // try to obtain the name of thie user
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;
                name = userDetails.getUsername();
            } else {
                name = principal.toString();
            }

            // check for the authority for feature admins
            boolean featureAdmin = false;
            if (featureAdminAuthority != null) {
                for (GrantedAuthority authority : authentication.getAuthorities()) {
                    if (authority.getAuthority().equals(featureAdminAuthority)) {
                        featureAdmin = true;
                    }
                }
            }

            return new SimpleFeatureUser(name, featureAdmin);

        }
        return null;
    }

}
