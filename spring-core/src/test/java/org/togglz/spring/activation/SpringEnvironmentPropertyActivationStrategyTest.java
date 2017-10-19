package org.togglz.spring.activation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.togglz.core.Feature;
import org.togglz.core.activation.AbstractPropertyDrivenActivationStrategy;
import org.togglz.core.activation.Parameter;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.util.Strings;
import org.togglz.spring.util.ContextClassLoaderApplicationContextHolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * <p>
 * Tests for the {@link SpringEnvironmentPropertyActivationStrategy} class.
 * </p>
 *
 * @author Alasdair Mercer
 */
@RunWith(Theories.class)
public class SpringEnvironmentPropertyActivationStrategyTest {

    @DataPoints
    public static final boolean[] DATA_POINTS = { true, false };

    @Mock
    private ApplicationContext mockApplicationContext;
    @Mock
    private Environment mockEnvironment;

    private SpringEnvironmentPropertyActivationStrategy strategy;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(mockApplicationContext.getEnvironment()).thenReturn(mockEnvironment);

        strategy = new SpringEnvironmentPropertyActivationStrategy();
    }

    @After
    public void tearDown() {
        ContextClassLoaderApplicationContextHolder.release();
    }

    @Test
    public void testGetId() {
        assertEquals(SpringEnvironmentPropertyActivationStrategy.ID, strategy.getId());
    }

    @Test
    public void testGetName() {
        assertTrue(Strings.isNotBlank(strategy.getName()));
    }

    @Theory
    public void testIsActiveWithNoParam(boolean enabled) {
        FeatureState featureState = new FeatureState(TestFeatures.FEATURE_ONE, !enabled);

        ContextClassLoaderApplicationContextHolder.bind(mockApplicationContext);

        when(mockEnvironment.getProperty("togglz.FEATURE_ONE")).thenReturn(String.valueOf(enabled));

        assertEquals(enabled, strategy.isActive(featureState, null));
    }

    @Theory
    public void testIsActiveWithParam(boolean enabled) {
        String paramValue = "foo";
        FeatureState featureState = new FeatureState(TestFeatures.FEATURE_ONE, !enabled);
        featureState.setParameter(SpringEnvironmentPropertyActivationStrategy.PARAM_NAME, paramValue);

        ContextClassLoaderApplicationContextHolder.bind(mockApplicationContext);

        when(mockEnvironment.getProperty(paramValue)).thenReturn(String.valueOf(enabled));

        assertEquals(enabled, strategy.isActive(featureState, null));
    }

    @Test(expected = IllegalStateException.class)
    public void testIsActiveThrowsWhenNoApplicationContext() {
        FeatureState featureState = new FeatureState(TestFeatures.FEATURE_ONE, true);

        strategy.isActive(featureState, null);
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
