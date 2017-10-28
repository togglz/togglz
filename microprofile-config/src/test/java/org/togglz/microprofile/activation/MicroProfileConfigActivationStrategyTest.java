package org.togglz.microprofile.activation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.togglz.core.Feature;
import org.togglz.core.activation.AbstractPropertyDrivenActivationStrategy;
import org.togglz.core.activation.Parameter;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.util.Strings;
import org.togglz.microprofile.TestConfigSourceProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * <p>
 * Tests for the {@link MicroProfileConfigActivationStrategy} class.
 * </p>
 *
 * @author John D. Ament, Alasdair Mercer
 */
@RunWith(Theories.class)
public class MicroProfileConfigActivationStrategyTest {

    @DataPoints
    public static final boolean[] DATA_POINTS = { true, false };

    private MicroProfileConfigActivationStrategy strategy;

    @Before
    public void setUp() {
        strategy = new MicroProfileConfigActivationStrategy();
    }

    @After
    public void tearDown() {
        TestConfigSourceProvider.TestConfigSource.INSTANCE.clearProperties();
    }

    @Test
    public void testGetId() {
        assertEquals(MicroProfileConfigActivationStrategy.ID, strategy.getId());
    }

    @Test
    public void testGetName() {
        assertTrue(Strings.isNotBlank(strategy.getName()));
    }

    @Theory
    public void testIsActiveWithNoParam(boolean enabled) {
        FeatureState featureState = new FeatureState(TestFeatures.FEATURE_ONE, !enabled);

        TestConfigSourceProvider.TestConfigSource.INSTANCE.putProperty("togglz.FEATURE_ONE", String.valueOf(enabled));

        assertEquals(enabled, strategy.isActive(featureState, null));
    }

    @Theory
    public void testIsActiveWithParam(boolean enabled) {
        String paramValue = "foo";
        FeatureState featureState = new FeatureState(TestFeatures.FEATURE_ONE, !enabled);
        featureState.setParameter(MicroProfileConfigActivationStrategy.PARAM_NAME, paramValue);

        TestConfigSourceProvider.TestConfigSource.INSTANCE.putProperty(paramValue, String.valueOf(enabled));

        assertEquals(enabled, strategy.isActive(featureState, null));
    }

    @Test
    public void testGetParameters() {
        Parameter[] parameters = strategy.getParameters();

        assertEquals(2, parameters.length);

        Parameter parameter = parameters[0];

        assertNotNull(parameter);
        assertEquals(AbstractPropertyDrivenActivationStrategy.PARAM_NAME, parameter.getName());
        assertTrue(parameter.isOptional());
        assertTrue(Strings.isNotBlank(parameter.getLabel()));
        assertTrue(Strings.isNotBlank(parameter.getDescription()));

        parameter = parameters[1];

        assertNotNull(parameter);
        assertEquals(AbstractPropertyDrivenActivationStrategy.PARAM_PROPERTY_VALUE, parameter.getName());
        assertTrue(parameter.isOptional());
        assertTrue(Strings.isNotBlank(parameter.getLabel()));
        assertTrue(Strings.isNotBlank(parameter.getDescription()));
    }

    public enum TestFeatures implements Feature {

        FEATURE_ONE
    }
}
