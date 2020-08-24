package org.togglz.microprofile.activation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.togglz.core.Feature;
import org.togglz.core.activation.AbstractPropertyDrivenActivationStrategy;
import org.togglz.core.activation.Parameter;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.util.Strings;
import org.togglz.microprofile.TestConfigSourceProvider;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <p>
 * Tests for the {@link MicroProfileConfigActivationStrategy} class.
 * </p>
 *
 * @author John D. Ament, Alasdair Mercer
 */
class MicroProfileConfigActivationStrategyTest {

    private MicroProfileConfigActivationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new MicroProfileConfigActivationStrategy();
    }

    @AfterEach
    void tearDown() {
        TestConfigSourceProvider.TestConfigSource.INSTANCE.clearProperties();
    }

    @Test
    void shouldHaveSameActivationStrategyId() {
        assertEquals(MicroProfileConfigActivationStrategy.ID, strategy.getId());
    }

    @Test
    void strategyNameShouldNotBeBlankByDefault() {
        assertTrue(Strings.isNotBlank(strategy.getName()));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void isActiveWithNoParam(boolean enabled) {
        FeatureState featureState = new FeatureState(TestFeatures.FEATURE_ONE, !enabled);

        TestConfigSourceProvider.TestConfigSource.INSTANCE.putProperty("togglz.FEATURE_ONE", String.valueOf(enabled));

        assertEquals(enabled, strategy.isActive(featureState, null));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void isActiveWithParam(boolean enabled) {
        String paramValue = "foo";
        FeatureState featureState = new FeatureState(TestFeatures.FEATURE_ONE, !enabled);
        featureState.setParameter(MicroProfileConfigActivationStrategy.PARAM_NAME, paramValue);

        TestConfigSourceProvider.TestConfigSource.INSTANCE.putProperty(paramValue, String.valueOf(enabled));

        assertEquals(enabled, strategy.isActive(featureState, null));
    }

    @Test
    void getParameters() {
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

    enum TestFeatures implements Feature {
        FEATURE_ONE
    }
}
