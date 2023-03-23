package org.togglz.spring.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;

import java.util.Set;

public class SpringSecurityUserProvider implements UserProvider {

    public static final String USER_ATTRIBUTE_ROLES = "roles";

    private final String featureAdminAuthority;

    public SpringSecurityUserProvider(String featureAdminAuthority) {
        this.featureAdminAuthority = featureAdminAuthority;
    }

    @Override
    public FeatureUser getCurrentUser() {
        Authentication authentication = createAuthentication();

        // null if no authentication data is available for the current thread
        if (authentication == null) {
            return null;
        }

        // try to obtain the name of this user
        String name = getUserName(authentication);

        // check for the authority for feature admins
        Set<String> authorities = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        boolean featureAdmin = isFeatureAdmin(authorities);

        SimpleFeatureUser user = new SimpleFeatureUser(name, featureAdmin);
        user.setAttribute(USER_ATTRIBUTE_ROLES, authorities);
        return user;
    }

    public static Authentication createAuthentication() {
        SecurityContext context = SecurityContextHolder.getContext();
        return context.getAuthentication();
    }

    protected boolean isFeatureAdmin(Set<String> authorities) {
        return featureAdminAuthority != null && authorities.contains(featureAdminAuthority);
    }

    private String getUserName(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return principal.toString();
        }
        UserDetails userDetails = (UserDetails) principal;
        return userDetails.getUsername();
    }
}
