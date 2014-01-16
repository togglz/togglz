package org.togglz.spring.security;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.togglz.spring.security.UserAuthorityActivationStrategy.ID;
import static org.togglz.spring.security.UserAuthorityActivationStrategy.NAME;
import static org.togglz.spring.security.UserAuthorityActivationStrategy.PARAM_AUTHORITIES_DESC;
import static org.togglz.spring.security.UserAuthorityActivationStrategy.PARAM_AUTHORITIES_LABEL;
import static org.togglz.spring.security.UserAuthorityActivationStrategy.PARAM_AUTHORITIES_NAME;
import static org.togglz.spring.security.UserAuthorityActivationStrategy.USER_ATTRIBUTE_AUTHORITIES;

import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.togglz.core.activation.Parameter;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.FeatureUser;

@RunWith(MockitoJUnitRunner.class)
public class UserAuthorityActivationStrategyTest {

    @InjectMocks
    private UserAuthorityActivationStrategy activationStrategy;

    @Mock
    private FeatureState state;
    @Mock
    private FeatureUser user;

    private Set<String> userAuthorities;

    @Before
    public void setUp() throws Exception {
        userAuthorities = new HashSet<String>();
    }

    @Test
    public void getIdWillReturnConstant() throws Exception {
        assertThat(activationStrategy.getId(), is(ID));
    }

    @Test
    public void getNameWillReturnConstant() throws Exception {
        assertThat(activationStrategy.getName(), is(NAME));
    }

    @Test
    public void getParametersWillReturnAuthorities() throws Exception {
        Parameter[] result = activationStrategy.getParameters();

        assertThat(result.length, is(1));

        Parameter param = result[0];
        assertThat(param.getName(), is(PARAM_AUTHORITIES_NAME));
        assertThat(param.getDescription(), is(PARAM_AUTHORITIES_DESC));
        assertThat(param.getLabel(), is(PARAM_AUTHORITIES_LABEL));
        assertThat(param.isLargeText(), is(true));
    }

    @Test
    public void isActiveWillReturnFalseWhenThereIsNoUser() throws Exception {
        boolean result = activationStrategy.isActive(state, null);

        assertThat(result, is(false));
    }

    @Test
    public void isActiveWillReturnFalseWhenThereIsNoAuthoritiesAttribute() throws Exception {
        when(user.getAttribute(USER_ATTRIBUTE_AUTHORITIES)).thenReturn(null);

        boolean result = activationStrategy.isActive(state, user);

        assertThat(result, is(false));
    }

    @Test
    public void isActiveWillReturnFalseWhenThereIsNoAuthoritiesParam() throws Exception {
        when(user.getAttribute(USER_ATTRIBUTE_AUTHORITIES)).thenReturn(userAuthorities);
        when(state.getParameter(PARAM_AUTHORITIES_NAME)).thenReturn(null);

        boolean result = activationStrategy.isActive(state, user);

        assertThat(result, is(false));
    }

    @Test
    public void isActiveWillReturnFalseWhenAuthoritiesParamIsBlank() throws Exception {
        when(user.getAttribute(USER_ATTRIBUTE_AUTHORITIES)).thenReturn(userAuthorities);
        when(state.getParameter(PARAM_AUTHORITIES_NAME)).thenReturn("   ");

        boolean result = activationStrategy.isActive(state, user);

        assertThat(result, is(false));
    }

    @Test
    public void isActiveWillReturnFalseWhenUserHasNoneOfSelectedAuthorities() throws Exception {
        when(user.getAttribute(USER_ATTRIBUTE_AUTHORITIES)).thenReturn(userAuthorities);
        when(state.getParameter(PARAM_AUTHORITIES_NAME)).thenReturn("ROLE_1, ROLE_2, ROLE_3");

        boolean result = activationStrategy.isActive(state, user);

        assertThat(result, is(false));
    }

    @Test
    public void isActiveWillReturnTrueWhenUserHasAnyOfSelectedAuthorities() throws Exception {
        userAuthorities.add("ROLE_2");
        when(user.getAttribute(USER_ATTRIBUTE_AUTHORITIES)).thenReturn(userAuthorities);
        when(state.getParameter(PARAM_AUTHORITIES_NAME)).thenReturn("ROLE_1, ROLE_2, ROLE_3");

        boolean result = activationStrategy.isActive(state, user);

        assertThat(result, is(true));
    }
}
