package org.togglz.core.activation;

import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class StringWhitelistActivationStrategyTest {

    private final StringWhitelistActivationStrategy strategy = new StringWhitelistActivationStrategy();

    @Test(expected = UnsupportedOperationException.class)
    public void deprecatedMethodShouldThrow() {
        //noinspection deprecation
        strategy.isActive(null, null);
    }

    @Test
    public void shouldReturnFalseForEmptyWhitelist() {

        FeatureState state = new FeatureState(MyFeature.FEATURE)
            .enable()
            .setStrategyId(StringWhitelistActivationStrategy.ID);

        boolean active = strategy.isActive(state, null, "ck");

        assertEquals(false, active);

    }

    @Test
    public void shouldReturnFalseForNullContext() {
        FeatureState state = new FeatureState(MyFeature.FEATURE)
            .enable()
            .setStrategyId(StringWhitelistActivationStrategy.ID)
            .setParameter(StringWhitelistActivationStrategy.PARAM_WHITELIST, "context1,ck,context2");

        boolean active = strategy.isActive(state, null, null);

        assertEquals(false, active);

    }

    @Test
    public void shouldReturnFalseForDifferentContext() {

        FeatureState state = new FeatureState(MyFeature.FEATURE)
            .enable()
            .setStrategyId(StringWhitelistActivationStrategy.ID)
            .setParameter(StringWhitelistActivationStrategy.PARAM_WHITELIST, "context1,ck,context2");

        boolean active = strategy.isActive(state, null, "!ck");

        assertEquals(false, active);

    }

    @Test
    public void shouldReturnTrueForCorrectContext() {

        FeatureState state = new FeatureState(MyFeature.FEATURE)
            .enable()
            .setStrategyId(StringWhitelistActivationStrategy.ID)
            .setParameter(StringWhitelistActivationStrategy.PARAM_WHITELIST, "context1,ck,context2");

        boolean active = strategy.isActive(state, null, "ck");

        assertEquals(true, active);

    }

    @Test
    public void shouldReturnCorrectParameterList() {

        Parameter[] parameters = strategy.getParameters();

        assertThat(parameters, notNullValue());
        assertThat(parameters.length, is(1));

        Parameter whitelistParam = parameters[0];

        assertThat(whitelistParam, notNullValue());
        assertThat(whitelistParam.getName(), is(StringWhitelistActivationStrategy.PARAM_WHITELIST));

    }

    private enum MyFeature implements Feature {
        FEATURE;
    }

}