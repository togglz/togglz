package org.togglz.core.activation;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.togglz.core.activation.UserRoleActivationStrategy.ID;
import static org.togglz.core.activation.UserRoleActivationStrategy.NAME;
import static org.togglz.core.activation.UserRoleActivationStrategy.PARAM_ROLES_DESC;
import static org.togglz.core.activation.UserRoleActivationStrategy.PARAM_ROLES_LABEL;
import static org.togglz.core.activation.UserRoleActivationStrategy.PARAM_ROLES_NAME;
import static org.togglz.core.activation.UserRoleActivationStrategy.USER_ATTRIBUTE_ROLES;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.FeatureUser;

@RunWith(MockitoJUnitRunner.class)
public class UserRoleActivationStrategyTest {

    @InjectMocks
    private UserRoleActivationStrategy activationStrategy;

    @Mock
    private FeatureState state;

    @Mock
    private FeatureUser user;

    private Set<String> userRoles;

    @Before
    public void setUp() throws Exception {
        userRoles = new HashSet<String>();
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
    public void getParametersWillReturnRoles() throws Exception {
        Parameter[] result = activationStrategy.getParameters();

        assertThat(result.length, is(1));

        Parameter param = result[0];
        assertThat(param.getName(), is(PARAM_ROLES_NAME));
        assertThat(param.getDescription(), is(PARAM_ROLES_DESC));
        assertThat(param.getLabel(), is(PARAM_ROLES_LABEL));
        assertThat(param.isLargeText(), is(true));
    }

    @Test
    public void isActiveWillReturnFalseWhenThereIsNoUser() throws Exception {
        boolean result = activationStrategy.isActive(state, null);

        assertThat(result, is(false));
    }

    @Test
    public void isActiveWillReturnFalseWhenThereIsNoRolesAttribute() throws Exception {
        Mockito.when(user.getAttribute(USER_ATTRIBUTE_ROLES)).thenReturn(null);

        boolean result = activationStrategy.isActive(state, user);

        assertThat(result, is(false));
    }

    @Test
    public void isActiveWillReturnFalseWhenThereIsNoRolesParam() throws Exception {
        Mockito.when(user.getAttribute(USER_ATTRIBUTE_ROLES)).thenReturn(userRoles);
        Mockito.when(state.getParameter(PARAM_ROLES_NAME)).thenReturn(null);

        boolean result = activationStrategy.isActive(state, user);

        assertThat(result, is(false));
    }

    @Test
    public void isActiveWillReturnFalseWhenRolesParamIsBlank() throws Exception {
        Mockito.when(user.getAttribute(USER_ATTRIBUTE_ROLES)).thenReturn(userRoles);
        Mockito.when(state.getParameter(PARAM_ROLES_NAME)).thenReturn("   ");

        boolean result = activationStrategy.isActive(state, user);

        assertThat(result, is(false));
    }

    @Test
    public void isActiveWillReturnFalseWhenUserHasNoneOfSelectedRoles() throws Exception {
        Mockito.when(user.getAttribute(USER_ATTRIBUTE_ROLES)).thenReturn(userRoles);
        Mockito.when(state.getParameter(PARAM_ROLES_NAME)).thenReturn("ROLE_1, ROLE_2, ROLE_3");

        boolean result = activationStrategy.isActive(state, user);

        assertThat(result, is(false));
    }

    @Test
    public void isActiveWillReturnTrueWhenUserHasAnyOfSelectedRoles() throws Exception {
        userRoles.add("ROLE_2");
        Mockito.when(user.getAttribute(USER_ATTRIBUTE_ROLES)).thenReturn(userRoles);
        Mockito.when(state.getParameter(PARAM_ROLES_NAME)).thenReturn("ROLE_1, ROLE_2, ROLE_3");

        boolean result = activationStrategy.isActive(state, user);

        assertThat(result, is(true));
    }

    @Test
    public void doesntFailForOtherCollectionTypes() {

        Collection<String> userRoles = new ArrayList<String>();
        userRoles.add("SOME_ROLE");

        Mockito.when(user.getAttribute(USER_ATTRIBUTE_ROLES)).thenReturn(userRoles);
        Mockito.when(state.getParameter(PARAM_ROLES_NAME)).thenReturn("SOME_ROLE");

        boolean result = activationStrategy.isActive(state, user);

        assertThat(result, is(true));

    }
}
