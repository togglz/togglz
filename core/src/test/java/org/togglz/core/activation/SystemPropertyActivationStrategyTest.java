package org.togglz.core.activation;

import org.junit.Before;
import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.SimpleFeatureUser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by ddcchrisk on 5/26/16.
 */
public class SystemPropertyActivationStrategyTest {

    private final SystemPropertyActivationStrategy strategy = new SystemPropertyActivationStrategy();
    private FeatureUser user = new SimpleFeatureUser("who-cares-what-my-name-is");
    private FeatureState state ;

    @Before
    public void setup() {
        System.clearProperty("foo.bar");
        setState("foo.bar", "true");
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
    public void shouldBeTrueIfPropertyStringsMatch() {
        setState("foo.bar", "foobar");
        System.setProperty("foo.bar", "foobar");
        assertTrue(strategy.isActive(state,user));
    }

    @Test
    public void shouldBeTrueIfPropertyAndStateValueMatchFalse() {
        setState("foo.bar", "false");
        System.setProperty("foo.bar", "false");
        assertTrue(strategy.isActive(state,user));
    }

    @Test
    public void shouldBeTrueIfPropertyExistsAndIsTrue() {
        System.setProperty("foo.bar", "true");
        assertTrue(strategy.isActive(state,user));
    }

    @Test
    public void shouldBeFalseIfPropertyExistsAndIsEmpty() {
        setState("foo.bar", "");
        System.setProperty("foo.bar", "");
        assertFalse(strategy.isActive(state,user));

    }

    @Test
    public void shouldBeFalseIfNoMatchingFeatureState() {
        System.setProperty("foo.bar", "true");
        setState("foo.baz", "true");
        assertFalse(strategy.isActive(state,user));

    }

    private void setState(String propName, String propValue) {
        state = new FeatureState(ScriptFeature.FEATURE)
                .setParameter(SystemPropertyActivationStrategy.PARAM_PROPERTY_NAME, propName)
                .setParameter(SystemPropertyActivationStrategy.PARAM_PROPERTY_VALUE, propValue)
                .setStrategyId(SystemPropertyActivationStrategy.ID);
    }

    private enum ScriptFeature implements Feature {
        FEATURE;
    }

}
