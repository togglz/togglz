package org.togglz.spring.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.SimpleFeatureUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.togglz.spring.security.SpringSecurityUserProvider.USER_ATTRIBUTE_ROLES;

public class SpringSecurityUserProviderTest {

    private SpringSecurityUserProvider admin;

    @BeforeEach
    public void setUp() {
        admin = new SpringSecurityUserProvider("ROLE_ADMIN");
    }

    @Test
    public void getCurrentUserWillReturnFeatureAdminWhenAuthoritiesContainFeatureAdminAuthority() {
        try (MockedStatic<SpringSecurityUserProvider> mockedStatic = mockStatic(SpringSecurityUserProvider.class)) {
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_1"));
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            authorities.add(new SimpleGrantedAuthority("ROLE_2"));

            mockedStatic.when(SpringSecurityUserProvider::createAuthentication).thenReturn(new MyAuthentication(authorities));
            final SpringSecurityUserProvider mock = mock(SpringSecurityUserProvider.class);
            when(mock.getCurrentUser()).thenReturn(new SimpleFeatureUser("bennetelli"));

            FeatureUser user = admin.getCurrentUser();

            // assert
            assertTrue(user.isFeatureAdmin());
        }
    }

    @Test
    public void getCurrentUserWillReturnNormalUserWhenAuthoritiesDoNotContainFeatureAdminAuthority() {
        try (MockedStatic<SpringSecurityUserProvider> mockedStatic = mockStatic(SpringSecurityUserProvider.class)) {
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_1"));
            authorities.add(new SimpleGrantedAuthority("ROLE_2"));

            mockedStatic.when(SpringSecurityUserProvider::createAuthentication).thenReturn(new MyAuthentication(authorities));
            final SpringSecurityUserProvider mock = mock(SpringSecurityUserProvider.class);
            when(mock.getCurrentUser()).thenReturn(new SimpleFeatureUser("bennetelli"));

            // act
            FeatureUser user = admin.getCurrentUser();

            // assert
            assertFalse(user.isFeatureAdmin());
        }
    }

    @Test
    public void getCurrentUserWillCopyAuthoritiesFromAuthenticationIntoFeatureUser() {
        try (MockedStatic<SpringSecurityUserProvider> mockedStatic = mockStatic(SpringSecurityUserProvider.class)) {
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_1"));
            authorities.add(new SimpleGrantedAuthority("ROLE_2"));

            mockedStatic.when(SpringSecurityUserProvider::createAuthentication).thenReturn(new MyAuthentication(authorities));
            final SpringSecurityUserProvider mock = mock(SpringSecurityUserProvider.class);
            when(mock.getCurrentUser()).thenReturn(new SimpleFeatureUser("bennetelli"));

            FeatureUser user = admin.getCurrentUser();

            // assert
            Object authoritiesAttr = user.getAttribute(USER_ATTRIBUTE_ROLES);
            assertTrue(authoritiesAttr instanceof Set);
            Set authSet = (Set) authoritiesAttr;

            assertNotNull(authSet);

            Set<String> authoritySet = (Set<String>) authSet;
            assertEquals(authoritySet.size(), 2);
            assertTrue(authoritySet.contains("ROLE_1"));
            assertTrue(authoritySet.contains("ROLE_2"));
        }
    }

    static class MyAuthentication implements Authentication {

        private final Collection<GrantedAuthority> authorities;

        public MyAuthentication(Collection<GrantedAuthority> authorities) {
            this.authorities = authorities;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return this.authorities;
        }

        @Override
        public Object getCredentials() {
            return null;
        }

        @Override
        public Object getDetails() {
            return null;
        }

        @Override
        public Object getPrincipal() {
            return new UserDetails() {
                @Override
                public Collection<? extends GrantedAuthority> getAuthorities() {
                    return null;
                }

                @Override
                public String getPassword() {
                    return null;
                }

                @Override
                public String getUsername() {
                    return "my-name";
                }

                @Override
                public boolean isAccountNonExpired() {
                    return false;
                }

                @Override
                public boolean isAccountNonLocked() {
                    return false;
                }

                @Override
                public boolean isCredentialsNonExpired() {
                    return false;
                }

                @Override
                public boolean isEnabled() {
                    return false;
                }
            };
        }

        @Override
        public boolean isAuthenticated() {
            return false;
        }

        @Override
        public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        }

        @Override
        public String getName() {
            return null;
        }
    }
}
