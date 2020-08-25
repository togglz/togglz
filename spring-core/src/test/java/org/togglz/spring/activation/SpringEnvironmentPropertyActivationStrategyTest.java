package org.togglz.spring.activation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * <p>
 * Tests for the {@link SpringEnvironmentPropertyActivationStrategy} class.
 * </p>
 *
 * @author Alasdair Mercer
 */
class SpringEnvironmentPropertyActivationStrategyTest {

    @Mock
    private ApplicationContext mockApplicationContext;

    @Mock
    private Environment mockEnvironment;

    private SpringEnvironmentPropertyActivationStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(mockApplicationContext.getEnvironment()).thenReturn(mockEnvironment);

        strategy = new SpringEnvironmentPropertyActivationStrategy();
    }

    @AfterEach
    void tearDown() {
        ContextClassLoaderApplicationContextHolder.release();
    }

    @Test
    void testGetId() {
        assertEquals(SpringEnvironmentPropertyActivationStrategy.ID, strategy.getId());
    }

    @Test
    void testGetName() {
        assertTrue(Strings.isNotBlank(strategy.getName()));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testIsActiveWithNoParam(boolean enabled) {
        FeatureState featureState = new FeatureState(TestFeatures.FEATURE_ONE, !enabled);

        ContextClassLoaderApplicationContextHolder.bind(mockApplicationContext);

        when(mockEnvironment.getProperty("togglz.FEATURE_ONE")).thenReturn(String.valueOf(enabled));

        assertEquals(enabled, strategy.isActive(featureState, null));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testIsActiveWithParam(boolean enabled) {
        String paramValue = "foo";
        FeatureState featureState = new FeatureState(TestFeatures.FEATURE_ONE, !enabled);
        featureState.setParameter(SpringEnvironmentPropertyActivationStrategy.PARAM_NAME, paramValue);

        ContextClassLoaderApplicationContextHolder.bind(mockApplicationContext);

        when(mockEnvironment.getProperty(paramValue)).thenReturn(String.valueOf(enabled));

        assertEquals(enabled, strategy.isActive(featureState, null));
    }

    @Test
    void testIsActiveThrowsWhenNoApplicationContext() {
        FeatureState featureState = new FeatureState(TestFeatures.FEATURE_ONE, true);

        assertThrows(IllegalStateException.class, () -> strategy.isActive(featureState, null));
    }

    @Test
    void testGetParameters() {
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
