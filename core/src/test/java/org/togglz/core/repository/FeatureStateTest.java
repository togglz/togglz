package org.togglz.core.repository;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.activation.UsernameActivationStrategy;

public class FeatureStateTest {

    @Test
    public void testSimpleFeatureState() {

        // initial state
        FeatureState state = new FeatureState(Features.FEATURE1);
        assertThat(state.isEnabled(), is(false));
        assertThat(state.getParameterNames(), empty());

        // enable a feature
        state.enable();
        assertThat(state.isEnabled(), is(true));

        // add a parameter
        state.setParameter("foo", "bar");
        assertThat(state.getParameterNames().size(), is(1));
        assertThat(state.getParameter("foo"), is("bar"));

        // remove the parameter
        state.setParameter("foo", null);
        assertThat(state.getParameterNames().size(), is(0));

    }

    @Test
    public void testOldUsersApiHandling() {

        // initial state
        FeatureState state = new FeatureState(Features.FEATURE1, true, Arrays.asList("ck", "admin"));
        assertThat(state.isEnabled(), is(true));
        assertThat(state.getParameterNames(), contains(UsernameActivationStrategy.PARAM_USERS));
        assertThat(state.getParameter(UsernameActivationStrategy.PARAM_USERS), is("ck,admin"));
        assertThat(state.getUsers(), contains("ck", "admin"));

        // add some other user
        state.addUser("tester");
        assertThat(state.getParameter(UsernameActivationStrategy.PARAM_USERS), is("ck,admin,tester"));
        assertThat(state.getUsers(), contains("ck", "admin", "tester"));

    }

    private static enum Features implements Feature {
        FEATURE1;
    }

}
