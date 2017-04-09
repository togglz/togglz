package org.togglz.deltaspike.activation;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.togglz.core.Feature;
import org.togglz.core.activation.Parameter;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.util.Strings;
import org.togglz.deltaspike.TestConfigSourceProvider;

/**
 * <p>
 * Tests for the {@link DeltaSpikePropertyActivationStrategy} class.
 * </p>
 *
 * @author Alasdair Mercer
 */
@RunWith(Theories.class)
public class DeltaSpikePropertyActivationStrategyTest {

    @DataPoints
    public static final boolean[] DATA_POINTS = { true, false };

    private DeltaSpikePropertyActivationStrategy strategy;

    @Before
    public void setUp() {
        strategy = new DeltaSpikePropertyActivationStrategy();
    }

    @After
    public void tearDown() {
        TestConfigSourceProvider.clearProperties();
    }

    @Test
    public void testGetId() {
        assertEquals(DeltaSpikePropertyActivationStrategy.ID, strategy.getId());
    }

    @Test
    public void testGetName() {
        assertTrue(Strings.isNotBlank(strategy.getName()));
    }

    @Theory
    public void testIsActiveWithNoParam(boolean enabled) {
        FeatureState featureState = new FeatureState(TestFeatures.FEATURE_ONE, !enabled);

        TestConfigSourceProvider.putProperty("togglz.FEATURE_ONE", String.valueOf(enabled));

        assertEquals(enabled, strategy.isActive(featureState, null));
    }

    @Theory
    public void testIsActiveWithParam(boolean enabled) {
        String paramValue = "foo";
        FeatureState featureState = new FeatureState(TestFeatures.FEATURE_ONE, !enabled);
        featureState.setParameter(DeltaSpikePropertyActivationStrategy.PARAM_NAME, paramValue);

        TestConfigSourceProvider.putProperty(paramValue, String.valueOf(enabled));

        assertEquals(enabled, strategy.isActive(featureState, null));
    }

    @Test
    public void testGetParameters() {
        Parameter[] parameters = strategy.getParameters();

        assertEquals(1, parameters.length);

        Parameter parameter = parameters[0];

        assertNotNull(parameter);
        assertEquals(DeltaSpikePropertyActivationStrategy.PARAM_NAME, parameter.getName());
        assertTrue(parameter.isOptional());
        assertTrue(Strings.isNotBlank(parameter.getLabel()));
        assertTrue(Strings.isNotBlank(parameter.getDescription()));
    }

    public enum TestFeatures implements Feature {

        FEATURE_ONE
    }
}
