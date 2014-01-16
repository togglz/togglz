package org.togglz.spring.security;

import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;


public class SpringSecurityUserProvider implements UserProvider {

    public static final String USER_ATTRIBUTE_AUTHORITIES = "authorities";

    private final String featureAdminAuthority;

    public SpringSecurityUserProvider(String featureAdminAuthority) {
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

            Set<String> authorities = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

            // check for the authority for feature admins
            boolean featureAdmin = false;
            if (featureAdminAuthority != null) {
                featureAdmin = authorities.contains(featureAdminAuthority);
            }

            SimpleFeatureUser user = new SimpleFeatureUser(name, featureAdmin);
            user.setAttribute(USER_ATTRIBUTE_AUTHORITIES, authorities);

            return user;

        }
        return null;
    }

}
