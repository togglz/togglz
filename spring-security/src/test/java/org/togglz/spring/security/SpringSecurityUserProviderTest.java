package org.togglz.spring.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.togglz.core.user.FeatureUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.togglz.spring.security.SpringSecurityUserProvider.USER_ATTRIBUTE_ROLES;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ SecurityContextHolder.class })
@PowerMockIgnore("javax.security.*")
public class SpringSecurityUserProviderTest {

    private SpringSecurityUserProvider userProvider;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Before
    public void setUp() {
        mockStatic(SecurityContextHolder.class);

        when(SecurityContextHolder.getContext()).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        when(authentication.getPrincipal()).thenReturn("principal");

        userProvider = new SpringSecurityUserProvider("ROLE_ADMIN");
    }

    @Test
    public void getCurrentUserWillReturnFeatureAdminWhenAuthoritiesContainFeatureAdminAuthority() {
        // arrange
        Collection authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_1"));
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        authorities.add(new SimpleGrantedAuthority("ROLE_2"));
        when(authentication.getAuthorities()).thenReturn(authorities);

        // act
        FeatureUser user = userProvider.getCurrentUser();

        // assert
        assertTrue(user.isFeatureAdmin());
    }

    @Test
    public void getCurrentUserWillReturnNormalUserWhenAuthoritiesDoNotContainFeatureAdminAuthority() {
        // arrange
        Collection authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_1"));
        authorities.add(new SimpleGrantedAuthority("ROLE_2"));
        when(authentication.getAuthorities()).thenReturn(authorities);

        // act
        FeatureUser user = userProvider.getCurrentUser();

        // assert
        assertFalse(user.isFeatureAdmin());
    }

    @Test
    public void getCurrentUserWillCopyAuthoritiesFromAuthenticationIntoFeatureUser() {
        // arrange
        Collection authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_1"));
        authorities.add(new SimpleGrantedAuthority("ROLE_2"));
        when(authentication.getAuthorities()).thenReturn(authorities);

        // act
        FeatureUser user = userProvider.getCurrentUser();

        // assert
        Object authoritiesAttr = user.getAttribute(USER_ATTRIBUTE_ROLES);
        assertTrue(authoritiesAttr instanceof Set);
        Set authSet = (Set) authoritiesAttr;

        assertNotNull(authSet);

        Set<String> authoritySet = (Set<String>) authSet;
        assertEquals(2, authoritySet.size());
        assertTrue(authoritySet.contains("ROLE_1"));
        assertTrue(authoritySet.contains("ROLE_2"));
    }
}
