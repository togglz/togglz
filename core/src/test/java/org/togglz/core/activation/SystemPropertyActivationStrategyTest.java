package org.togglz.core.activation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.SimpleFeatureUser;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by ddcchrisk on 5/26/16.
 */
class SystemPropertyActivationStrategyTest {

    private final SystemPropertyActivationStrategy strategy = new SystemPropertyActivationStrategy();
    private final FeatureUser user = new SimpleFeatureUser("who-cares-what-my-name-is");
    private FeatureState state ;

    @BeforeEach
    void setUp() {
        System.clearProperty("foo.bar");
        setState("foo.bar", "true");
    }

    @Test
    void shouldBeFalseIfPropertyDoesNotExist() {
        assertFalse(strategy.isActive(state,user));

    }

    @Test
    void shouldBeFalseIfPropertyExistsButisFalse() {
        System.setProperty("foo.bar", "false");
        assertFalse(strategy.isActive(state,user));
    }

    @Test
    void shouldBeFalseIfPropertyExistsButisInvalid() {
        System.setProperty("foo.bar", "foobar");
        assertFalse(strategy.isActive(state,user));
    }

    @Test
    void shouldBeTrueIfPropertyStringsMatch() {
        setState("foo.bar", "foobar");
        System.setProperty("foo.bar", "foobar");
        assertTrue(strategy.isActive(state,user));
    }

    @Test
    void shouldBeTrueIfPropertyAndStateValueMatchFalse() {
        setState("foo.bar", "false");
        System.setProperty("foo.bar", "false");
        assertTrue(strategy.isActive(state,user));
    }

    @Test
    void shouldBeTrueIfPropertyExistsAndIsTrue() {
        System.setProperty("foo.bar", "true");
        assertTrue(strategy.isActive(state,user));
    }

    @Test
    void shouldBeFalseIfPropertyExistsAndIsEmpty() {
        setState("foo.bar", "");
        System.setProperty("foo.bar", "");
        assertFalse(strategy.isActive(state,user));

    }

    @Test
    void shouldBeFalseIfNoMatchingFeatureState() {
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
