package org.togglz.core.activation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.FeatureUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.togglz.core.activation.UserRoleActivationStrategy.*;

@ExtendWith(MockitoExtension.class)
class UserRoleActivationStrategyTest {

    @InjectMocks
    private UserRoleActivationStrategy activationStrategy;

    @Mock
    private FeatureState state;

    @Mock
    private FeatureUser user;

    private Set<String> userRoles;

    @BeforeEach
    public void setUp() {
        userRoles = new HashSet<>();
    }

    @Test
    void getIdWillReturnConstant() {
        assertEquals(ID, activationStrategy.getId());
    }

    @Test
    void getNameWillReturnConstant() {
        assertEquals(NAME, activationStrategy.getName());
    }

    @Test
    void getParametersWillReturnRoles() {
        Parameter[] result = activationStrategy.getParameters();

        assertEquals(1, result.length);

        Parameter param = result[0];
        assertEquals(PARAM_ROLES_NAME, param.getName());
        assertEquals(PARAM_ROLES_DESC, param.getDescription());
        assertEquals(PARAM_ROLES_LABEL, param.getLabel());
        assertTrue(param.isLargeText());
    }

    @Test
    void isActiveWillReturnFalseWhenThereIsNoUser() {
        boolean result = activationStrategy.isActive(state, null);

        assertFalse(result);
    }

    @Test
    void isActiveWillReturnFalseWhenThereIsNoRolesAttribute() {
        when(user.getAttribute(USER_ATTRIBUTE_ROLES)).thenReturn(null);

        boolean result = activationStrategy.isActive(state, user);

        assertFalse(result);
    }

    @Test
    void isActiveWillReturnFalseWhenThereIsNoRolesParam() {
        when(user.getAttribute(USER_ATTRIBUTE_ROLES)).thenReturn(userRoles);
        when(state.getParameter(PARAM_ROLES_NAME)).thenReturn(null);

        boolean result = activationStrategy.isActive(state, user);

        assertFalse(result);
    }

    @Test
    void isActiveWillReturnFalseWhenRolesParamIsBlank() {
        when(user.getAttribute(USER_ATTRIBUTE_ROLES)).thenReturn(userRoles);
        when(state.getParameter(PARAM_ROLES_NAME)).thenReturn("   ");

        boolean result = activationStrategy.isActive(state, user);

        assertFalse(result);
    }

    @Test
    void isActiveWillReturnFalseWhenUserHasNoneOfSelectedRoles() {
        when(user.getAttribute(USER_ATTRIBUTE_ROLES)).thenReturn(userRoles);
        when(state.getParameter(PARAM_ROLES_NAME)).thenReturn("ROLE_1, ROLE_2, ROLE_3");

        boolean result = activationStrategy.isActive(state, user);

        assertFalse(result);
    }

    @Test
    void isActiveWillReturnTrueWhenUserHasAnyOfSelectedRoles() {
        userRoles.add("ROLE_2");
        when(user.getAttribute(USER_ATTRIBUTE_ROLES)).thenReturn(userRoles);
        when(state.getParameter(PARAM_ROLES_NAME)).thenReturn("ROLE_1, ROLE_2, ROLE_3");

        boolean result = activationStrategy.isActive(state, user);

        assertTrue(result);
    }

    @Test
    void doesntFailForOtherCollectionTypes() {
        Collection<String> userRoles = new ArrayList<>();
        userRoles.add("SOME_ROLE");

        when(user.getAttribute(USER_ATTRIBUTE_ROLES)).thenReturn(userRoles);
        when(state.getParameter(PARAM_ROLES_NAME)).thenReturn("SOME_ROLE");

        boolean result = activationStrategy.isActive(state, user);

        assertTrue(result);

    }
}
