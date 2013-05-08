package org.togglz.core.activation;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.SimpleFeatureUser;

public class UsernameActivationStrategyTest {

    private final UsernameActivationStrategy strategy = new UsernameActivationStrategy();

    @Test
    public void shouldReturnFalseForEmptyUserlist() {

        FeatureUser user = new SimpleFeatureUser("ck", false);
        FeatureState state = new FeatureState(MyFeature.FEATURE)
            .enable()
            .setStrategyId(UsernameActivationStrategy.ID);

        boolean active = strategy.isActive(state, user);

        assertEquals(false, active);

    }

    @Test
    public void shouldReturnFalseForUnknownUser() {

        FeatureUser user = null;
        FeatureState state = new FeatureState(MyFeature.FEATURE)
            .enable()
            .setStrategyId(UsernameActivationStrategy.ID)
            .setParameter(UsernameActivationStrategy.PARAM_USERS, "person1,ck,person2");

        boolean active = strategy.isActive(state, user);

        assertEquals(false, active);

    }

    @Test
    public void shouldReturnFalseForDifferentUser() {

        FeatureUser user = new SimpleFeatureUser("john", false);
        FeatureState state = new FeatureState(MyFeature.FEATURE)
            .enable()
            .setStrategyId(UsernameActivationStrategy.ID)
            .setParameter(UsernameActivationStrategy.PARAM_USERS, "person1,ck,person2");

        boolean active = strategy.isActive(state, user);

        assertEquals(false, active);

    }

    @Test
    public void shouldReturnTrueForCorrectUser() {

        FeatureUser user = new SimpleFeatureUser("ck", false);
        FeatureState state = new FeatureState(MyFeature.FEATURE)
            .enable()
            .setStrategyId(UsernameActivationStrategy.ID)
            .setParameter(UsernameActivationStrategy.PARAM_USERS, "person1,ck,person2");

        boolean active = strategy.isActive(state, user);

        assertEquals(true, active);

    }

    @Test
    public void shouldReturnCorrectParameterList() {

        Parameter[] parameters = strategy.getParameters();

        assertThat(parameters, notNullValue());
        assertThat(parameters.length, is(1));

        Parameter userParam = parameters[0];

        assertThat(userParam, notNullValue());
        assertThat(userParam.getName(), is(UsernameActivationStrategy.PARAM_USERS));

    }

    private enum MyFeature implements Feature {
        FEATURE;
    }

}
