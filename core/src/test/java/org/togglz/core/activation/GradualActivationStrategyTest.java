package org.togglz.core.activation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.metadata.FeatureRuntimeAttributes;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.SimpleFeatureUser;

public class GradualActivationStrategyTest {

    private final ActivationStrategy strategy = new TestingGradualActivationStrategy();

    private FeatureRuntimeAttributes runtimeAttributes = new FeatureRuntimeAttributes();

    @Test
    public void shouldAlwaysReturnFalseForZeroPercent() {

        FeatureState state = new FeatureState(GradualFeature.FEATURE);
        state.setEnabled(true);
        state.setParameter(GradualActivationStrategy.PARAM_PERCENTAGE, "0");

        // whatever the hash value is, false is expected
        assertFalse(strategy.isActive(state, aUserWithHash(0), runtimeAttributes));
        assertFalse(strategy.isActive(state, aUserWithHash(1), runtimeAttributes));
        assertFalse(strategy.isActive(state, aUserWithHash(3), runtimeAttributes));
        assertFalse(strategy.isActive(state, aUserWithHash(10), runtimeAttributes));
        assertFalse(strategy.isActive(state, aUserWithHash(99), runtimeAttributes));
        assertFalse(strategy.isActive(state, aUserWithHash(100), runtimeAttributes));
        assertFalse(strategy.isActive(state, aUserWithHash(110), runtimeAttributes));

    }

    @Test
    public void shouldAlwaysReturnTrueForOneHundredPercent() {

        FeatureState state = new FeatureState(GradualFeature.FEATURE);
        state.setEnabled(true);
        state.setParameter(GradualActivationStrategy.PARAM_PERCENTAGE, "100");

        // whatever the hash value is, true is expected
        assertTrue(strategy.isActive(state, aUserWithHash(0), runtimeAttributes));
        assertTrue(strategy.isActive(state, aUserWithHash(1), runtimeAttributes));
        assertTrue(strategy.isActive(state, aUserWithHash(3), runtimeAttributes));
        assertTrue(strategy.isActive(state, aUserWithHash(10), runtimeAttributes));
        assertTrue(strategy.isActive(state, aUserWithHash(99), runtimeAttributes));
        assertTrue(strategy.isActive(state, aUserWithHash(100), runtimeAttributes));
        assertTrue(strategy.isActive(state, aUserWithHash(110), runtimeAttributes));

    }

    @Test
    public void shouldWorkCorrectlyForOnePercent() {

        FeatureState state = new FeatureState(GradualFeature.FEATURE);
        state.setEnabled(true);
        state.setParameter(GradualActivationStrategy.PARAM_PERCENTAGE, "1");

        // every value with % 100 == 0 will be active, which is exactly 1%
        assertTrue(strategy.isActive(state, aUserWithHash(0), runtimeAttributes));
        assertTrue(strategy.isActive(state, aUserWithHash(100), runtimeAttributes));

        // all other values result in false
        assertFalse(strategy.isActive(state, aUserWithHash(1), runtimeAttributes));
        assertFalse(strategy.isActive(state, aUserWithHash(3), runtimeAttributes));
        assertFalse(strategy.isActive(state, aUserWithHash(10), runtimeAttributes));
        assertFalse(strategy.isActive(state, aUserWithHash(99), runtimeAttributes));
        assertFalse(strategy.isActive(state, aUserWithHash(110), runtimeAttributes));

    }

    @Test
    public void shouldWorkCorrectlyForNinetyNinePercent() {

        FeatureState state = new FeatureState(GradualFeature.FEATURE);
        state.setEnabled(true);
        state.setParameter(GradualActivationStrategy.PARAM_PERCENTAGE, "99");

        // most values result in true
        assertTrue(strategy.isActive(state, aUserWithHash(0), runtimeAttributes));
        assertTrue(strategy.isActive(state, aUserWithHash(1), runtimeAttributes));
        assertTrue(strategy.isActive(state, aUserWithHash(3), runtimeAttributes));
        assertTrue(strategy.isActive(state, aUserWithHash(10), runtimeAttributes));
        assertTrue(strategy.isActive(state, aUserWithHash(98), runtimeAttributes));
        assertTrue(strategy.isActive(state, aUserWithHash(100), runtimeAttributes));

        // only 1% should result in false
        assertFalse(strategy.isActive(state, aUserWithHash(99), runtimeAttributes));
        assertFalse(strategy.isActive(state, aUserWithHash(199), runtimeAttributes));

    }

    @Test
    public void shouldFindCorrectDecisionForIntermediateValues() {

        FeatureState state = new FeatureState(GradualFeature.FEATURE);
        state.setEnabled(true);
        state.setParameter(GradualActivationStrategy.PARAM_PERCENTAGE, "50");

        // for hash values 0-49 the feature is active
        assertTrue(strategy.isActive(state, aUserWithHash(0), runtimeAttributes));
        assertTrue(strategy.isActive(state, aUserWithHash(25), runtimeAttributes));
        assertTrue(strategy.isActive(state, aUserWithHash(49), runtimeAttributes));

        // for hash values 50-99 the feaute is active
        assertFalse(strategy.isActive(state, aUserWithHash(50), runtimeAttributes));
        assertFalse(strategy.isActive(state, aUserWithHash(99), runtimeAttributes));

    }

    @Test
    public void shouldReturnFalseForInvalidPercentage() {

        FeatureState state = new FeatureState(GradualFeature.FEATURE);
        state.setEnabled(true);
        state.setParameter(GradualActivationStrategy.PARAM_PERCENTAGE, "100x");

        assertFalse(strategy.isActive(state, aUserWithHash(0), runtimeAttributes));
        assertFalse(strategy.isActive(state, aUserWithHash(99), runtimeAttributes));

    }

    private FeatureUser aUserWithHash(int hash) {
        return new SimpleFeatureUser("hash-" + hash, false);
    }

    private enum GradualFeature implements Feature {
        FEATURE;
    }

    private class TestingGradualActivationStrategy extends GradualActivationStrategy {

        private final Pattern HASH_PATTERN = Pattern.compile("^hash\\-(\\d+)$");;

        @Override
        protected int calculateHashCode(FeatureUser user) {
            Matcher matcher = HASH_PATTERN.matcher(user.getName());
            if (matcher.matches()) {
                return Integer.valueOf(matcher.group(1));
            }
            return super.calculateHashCode(user);
        }

    }
}
