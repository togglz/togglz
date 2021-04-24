package org.togglz.core.activation;

import org.junit.jupiter.api.Test;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.SimpleFeatureUser;

import static org.junit.jupiter.api.Assertions.*;

class UsernameActivationStrategyTest {

    private final UsernameActivationStrategy strategy = new UsernameActivationStrategy();

    @Test
    void shouldReturnFalseForEmptyUserlist() {
        FeatureUser user = new SimpleFeatureUser("ck", false);
        FeatureState state = new FeatureState(MyFeature.FEATURE)
            .enable()
            .setStrategyId(UsernameActivationStrategy.ID);

        boolean active = strategy.isActive(state, user);

        assertFalse(active);

    }

    @Test
    void shouldReturnFalseForUnknownUser() {
        FeatureUser user = null;
        FeatureState state = new FeatureState(MyFeature.FEATURE)
            .enable()
            .setStrategyId(UsernameActivationStrategy.ID)
            .setParameter(UsernameActivationStrategy.PARAM_USERS, "person1,ck,person2");

        boolean active = strategy.isActive(state, user);

        assertFalse(active);
    }

    @Test
    void shouldReturnFalseForDifferentUser() {
        FeatureUser user = new SimpleFeatureUser("john", false);
        FeatureState state = new FeatureState(MyFeature.FEATURE)
            .enable()
            .setStrategyId(UsernameActivationStrategy.ID)
            .setParameter(UsernameActivationStrategy.PARAM_USERS, "person1,ck,person2");

        boolean active = strategy.isActive(state, user);

        assertFalse(active);

    }

    @Test
    void shouldReturnTrueForCorrectUser() {
        FeatureUser user = new SimpleFeatureUser("ck", false);
        FeatureState state = new FeatureState(MyFeature.FEATURE)
            .enable()
            .setStrategyId(UsernameActivationStrategy.ID)
            .setParameter(UsernameActivationStrategy.PARAM_USERS, "person1,ck,person2");

        boolean active = strategy.isActive(state, user);

        assertTrue(active);

    }

    @Test
    void shouldReturnCorrectParameterList() {
        Parameter[] parameters = strategy.getParameters();

        assertNotNull(parameters);
        assertEquals(1, parameters.length);

        Parameter userParam = parameters[0];

        assertNotNull(userParam);
        assertEquals(UsernameActivationStrategy.PARAM_USERS, userParam.getName());
    }

    private enum MyFeature implements Feature {
        FEATURE;
    }
}
