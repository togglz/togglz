package org.togglz.core.repository;

import org.junit.jupiter.api.Test;
import org.togglz.core.Feature;
import org.togglz.core.activation.UsernameActivationStrategy;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeatureStateTest {

    @Test
    void testSimpleFeatureState() {

        // initial state
        FeatureState state = new FeatureState(Features.FEATURE1);
        assertFalse(state.isEnabled());
        assertEquals(0, state.getParameterNames().size());

        // enable a feature
        state.enable();
        assertTrue(state.isEnabled());

        // add a parameter
        state.setParameter("foo", "bar");
        assertEquals(1, state.getParameterNames().size());
        assertEquals("bar", state.getParameter("foo"));

        // remove the parameter
        state.setParameter("foo", null);
        assertEquals(0, state.getParameterNames().size());
    }

    @Test
    void testOldUsersApiHandling() {

        // initial state
        FeatureState state = new FeatureState(Features.FEATURE1, true, Arrays.asList("ck", "admin"));
        assertTrue(state.isEnabled());
        assertTrue(state.getParameterNames().contains(UsernameActivationStrategy.PARAM_USERS));
        assertEquals("ck,admin", state.getParameter(UsernameActivationStrategy.PARAM_USERS));
        assertTrue(state.getUsers().containsAll(Arrays.asList("ck", "admin")));

        // add some other user
        state.addUser("tester");
        assertEquals("ck,admin,tester", state.getParameter(UsernameActivationStrategy.PARAM_USERS));
        assertEquals(state.getUsers().get(0), "ck");
        assertEquals(state.getUsers().get(1), "admin");
        assertEquals(state.getUsers().get(2), "tester");
    }

    @Test
    void testEquals() {

        FeatureState state = new FeatureState(Features.FEATURE1);
        FeatureState copy = state.copy();
        assertEquals(state, copy);

        // Different feature
        assertNotEquals(state, new FeatureState(Features.FEATURE2));

        // Different enabled state
        state.setEnabled(true);
        assertNotEquals(state, copy);

        // Different strategy
        copy = state.copy();
        state.setStrategyId("Strategy");
        assertNotEquals(state, copy);

        // Different parameters
        copy = state.copy();
        state.setParameter("key", "value");
        assertNotEquals(state, copy);
    }

    private enum Features implements Feature {
        FEATURE1,
        FEATURE2
    }
}
