package org.togglz.core.activation;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.SimpleFeatureUser;

/**
 * Created by ddcchrisk on 5/26/16.
 */
public class PropertyActivationStrategyTest {

    private final PropertyActivationStrategy strategy = new PropertyActivationStrategy();
    private FeatureUser user = new SimpleFeatureUser("who-cares-what-my-name-is");
    private FeatureState state ;

    @Before
    public void setup() {
        setState("foo.bar");
    }

    @Test
    public void shouldBeFalseIfPropertyDoesNotExist() {
        //no property exists
        assertFalse(strategy.isActive(state,user));

    }

    @Test
    public void shouldBeFalseIfPropertyExistsButisFalse() {
        System.setProperty("foo.bar", "false");
        assertFalse(strategy.isActive(state,user));
    }

    @Test
    public void shouldBeFalseIfPropertyExistsButisInvalid() {
        System.setProperty("foo.bar", "foobar");
        assertFalse(strategy.isActive(state,user));
    }

    @Test
    public void shouldBeTrueIfPropertyExistsAndIsTrue() {
        System.setProperty("foo.bar", "true");
        assertTrue(strategy.isActive(state,user));
    }

    @Test
    public void shouldBeFalseIfPropertyExistsAndIsEmpty() {
        System.setProperty("foo.bar", "");
        assertFalse(strategy.isActive(state,user));

    }

    @Test
    public void shouldBeFalseIfNoMatchingFeatureState() {
        System.setProperty("foo.bar", "true");
        setState("foo.baz");
        assertFalse(strategy.isActive(state,user));

    }

    private void setState(String prop) {
        state = new FeatureState(ScriptFeature.FEATURE)
                .setParameter(PropertyActivationStrategy.PARAM_PROPERTY, prop)
                .setStrategyId(PropertyActivationStrategy.ID);
    }

    private enum ScriptFeature implements Feature {
        FEATURE;
    }

}
