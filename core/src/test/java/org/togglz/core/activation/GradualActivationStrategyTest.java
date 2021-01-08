package org.togglz.core.activation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.SimpleFeatureUser;

import static org.junit.jupiter.api.Assertions.*;

class GradualActivationStrategyTest {

    private final ActivationStrategy strategy = new TestingGradualActivationStrategy();

    @Test
    void shouldAlwaysReturnFalseForZeroPercent() {
        FeatureState state = new FeatureState(GradualFeature.FEATURE);
        state.setEnabled(true);
        state.setParameter(GradualActivationStrategy.PARAM_PERCENTAGE, "0");

        // whatever the hash value is, false is expected
        assertFalse(strategy.isActive(state, aUserWithHash(0)));
        assertFalse(strategy.isActive(state, aUserWithHash(1)));
        assertFalse(strategy.isActive(state, aUserWithHash(3)));
        assertFalse(strategy.isActive(state, aUserWithHash(10)));
        assertFalse(strategy.isActive(state, aUserWithHash(99)));
        assertFalse(strategy.isActive(state, aUserWithHash(100)));
        assertFalse(strategy.isActive(state, aUserWithHash(110)));

    }

    @Test
    void shouldAlwaysReturnTrueForOneHundredPercent() {
        FeatureState state = new FeatureState(GradualFeature.FEATURE);
        state.setEnabled(true);
        state.setParameter(GradualActivationStrategy.PARAM_PERCENTAGE, "100");

        // whatever the hash value is, true is expected
        assertTrue(strategy.isActive(state, aUserWithHash(0)));
        assertTrue(strategy.isActive(state, aUserWithHash(1)));
        assertTrue(strategy.isActive(state, aUserWithHash(3)));
        assertTrue(strategy.isActive(state, aUserWithHash(10)));
        assertTrue(strategy.isActive(state, aUserWithHash(99)));
        assertTrue(strategy.isActive(state, aUserWithHash(100)));
        assertTrue(strategy.isActive(state, aUserWithHash(110)));

    }

    @Test
    void shouldWorkCorrectlyForOnePercent() {
        FeatureState state = new FeatureState(GradualFeature.FEATURE);
        state.setEnabled(true);
        state.setParameter(GradualActivationStrategy.PARAM_PERCENTAGE, "1");

        // every value with % 100 == 0 will be active, which is exactly 1%
        assertTrue(strategy.isActive(state, aUserWithHash(0)));
        assertTrue(strategy.isActive(state, aUserWithHash(100)));

        // all other values result in false
        assertFalse(strategy.isActive(state, aUserWithHash(1)));
        assertFalse(strategy.isActive(state, aUserWithHash(3)));
        assertFalse(strategy.isActive(state, aUserWithHash(10)));
        assertFalse(strategy.isActive(state, aUserWithHash(99)));
        assertFalse(strategy.isActive(state, aUserWithHash(110)));

    }

    @Test
    void shouldWorkCorrectlyForNinetyNinePercent() {
        FeatureState state = new FeatureState(GradualFeature.FEATURE);
        state.setEnabled(true);
        state.setParameter(GradualActivationStrategy.PARAM_PERCENTAGE, "99");

        // most values result in true
        assertTrue(strategy.isActive(state, aUserWithHash(0)));
        assertTrue(strategy.isActive(state, aUserWithHash(1)));
        assertTrue(strategy.isActive(state, aUserWithHash(3)));
        assertTrue(strategy.isActive(state, aUserWithHash(10)));
        assertTrue(strategy.isActive(state, aUserWithHash(98)));
        assertTrue(strategy.isActive(state, aUserWithHash(100)));

        // only 1% should result in false
        assertFalse(strategy.isActive(state, aUserWithHash(99)));
        assertFalse(strategy.isActive(state, aUserWithHash(199)));

    }

    @Test
    void shouldFindCorrectDecisionForIntermediateValues() {
        FeatureState state = new FeatureState(GradualFeature.FEATURE);
        state.setEnabled(true);
        state.setParameter(GradualActivationStrategy.PARAM_PERCENTAGE, "50");

        // for hash values 0-49 the feature is active
        assertTrue(strategy.isActive(state, aUserWithHash(0)));
        assertTrue(strategy.isActive(state, aUserWithHash(25)));
        assertTrue(strategy.isActive(state, aUserWithHash(49)));

        // for hash values 50-99 the feature is active
        assertFalse(strategy.isActive(state, aUserWithHash(50)));
        assertFalse(strategy.isActive(state, aUserWithHash(99)));

    }

    @Test
    void shouldReturnFalseForInvalidPercentage() {
        FeatureState state = new FeatureState(GradualFeature.FEATURE);
        state.setEnabled(true);
        state.setParameter(GradualActivationStrategy.PARAM_PERCENTAGE, "100x");

        assertFalse(strategy.isActive(state, aUserWithHash(0)));
        assertFalse(strategy.isActive(state, aUserWithHash(99)));

    }

    private FeatureUser aUserWithHash(int hash) {
        return new SimpleFeatureUser("hash-" + hash, false);
    }

    private enum GradualFeature implements Feature {
        FEATURE
    }

    private static class TestingGradualActivationStrategy extends GradualActivationStrategy {

        private final Pattern HASH_PATTERN = Pattern.compile("^hash-(\\d+)$");

        @Override
        protected int calculateHashCode(FeatureUser user, Feature feature) {
            Matcher matcher = HASH_PATTERN.matcher(user.getName());
            if (matcher.matches()) {
                return Integer.parseInt(matcher.group(1));
            }
            return super.calculateHashCode(user, feature);
        }

    }
}
