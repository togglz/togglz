package org.togglz.spring.security;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.togglz.spring.security.SpringSecurityUserProvider.USER_ATTRIBUTE_ROLES;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.togglz.core.user.FeatureUser;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ SecurityContextHolder.class })
public class SpringSecurityUserProviderTest {

    private SpringSecurityUserProvider userProvider;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Before
    public void setUp() throws Exception {
        mockStatic(SecurityContextHolder.class);

        when(SecurityContextHolder.getContext()).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        when(authentication.getPrincipal()).thenReturn("principal");

        userProvider = new SpringSecurityUserProvider("ROLE_ADMIN");
    }

    @Test
    public void getCurrentUserWillReturnFeatureAdminWhenAuthoritiesContainFeatureAdminAuthority() throws Exception {
        // arrange
        Collection authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_1"));
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        authorities.add(new SimpleGrantedAuthority("ROLE_2"));
        when(authentication.getAuthorities()).thenReturn(authorities);

        // act
        FeatureUser user = userProvider.getCurrentUser();

        // assert
        assertThat(user.isFeatureAdmin(), is(true));
    }

    @Test
    public void getCurrentUserWillReturnNormalUserWhenAuthoritiesDoNotContainFeatureAdminAuthority() throws Exception {
        // arrange
        Collection authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_1"));
        authorities.add(new SimpleGrantedAuthority("ROLE_2"));
        when(authentication.getAuthorities()).thenReturn(authorities);

        // act
        FeatureUser user = userProvider.getCurrentUser();

        // assert
        assertThat(user.isFeatureAdmin(), is(false));
    }

    @Test
    public void getCurrentUserWillCopyAuthoritiesFromAuthenticationIntoFeatureUser() throws Exception {
        // arrange
        Collection authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_1"));
        authorities.add(new SimpleGrantedAuthority("ROLE_2"));
        when(authentication.getAuthorities()).thenReturn(authorities);

        // act
        FeatureUser user = userProvider.getCurrentUser();

        // assert
        Object authoritiesAttr = user.getAttribute(USER_ATTRIBUTE_ROLES);

        assertThat(authoritiesAttr, notNullValue());
        assertThat(authoritiesAttr, is(Set.class));

        Set<String> authoritySet = (Set<String>) authoritiesAttr;
        assertThat(authoritySet.size(), is(2));
        assertThat(authoritySet.contains("ROLE_1"), is(true));
        assertThat(authoritySet.contains("ROLE_2"), is(true));
    }
}
