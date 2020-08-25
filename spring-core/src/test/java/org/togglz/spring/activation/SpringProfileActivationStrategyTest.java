package org.togglz.spring.activation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.togglz.core.Feature;
import org.togglz.core.activation.AbstractTokenizedActivationStrategy.TokenTransformer;
import org.togglz.core.activation.Parameter;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.util.Strings;
import org.togglz.spring.util.ContextClassLoaderApplicationContextHolder;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * <p>
 * Tests for the {@link SpringProfileActivationStrategy} class.
 * </p>
 *
 * @author Alasdair Mercer
 */
class SpringProfileActivationStrategyTest {

    private static Stream<Arguments> activeTestCases() {
        return Stream.of(
                Arguments.of(new IsActiveTestCase("profile is not active", false, "p1", "p2")),
                Arguments.of(new IsActiveTestCase("profile is not active but param is negated", true, "!p1", "p2")),
                Arguments.of(new IsActiveTestCase("profile is active", true, "p1", "p1")),
                Arguments.of(new IsActiveTestCase("profile is active and param case is different", true, "p1", "P1")),
                Arguments.of(new IsActiveTestCase("profile is active and param case is different (inverted)", true, "P1", "p1")),
                Arguments.of(new IsActiveTestCase("profile is active but param is negated", false, "!p1", "p1")),
                Arguments.of(new IsActiveTestCase("none of profiles are active", false, "p1,p2,p3", "p4")),
                Arguments.of(new IsActiveTestCase("none of profiles are active but at least one param is negated", true, "p1,p2,!p3", "p4")),
                Arguments.of(new IsActiveTestCase("at least one profile is active", true, "p1,p2,p3", "p3", "p4")),
                Arguments.of(new IsActiveTestCase("all profiles are active", true, "p1,p2,p3", "p1", "p2", "p3", "p4")),
                Arguments.of(new IsActiveTestCase("none of profiles are active but all params are negated", true, "!p1,!p2,!p3", "p4")),
                Arguments.of(new IsActiveTestCase("no profiles are active", false, "p1")),
                Arguments.of(new IsActiveTestCase("no profiles are active but param is negated", true, "!p1")));
    }

    private String[] activeProfiles;

    @Mock
    private ApplicationContext mockApplicationContext;

    @Mock
    private Environment mockEnvironment;

    private SpringProfileActivationStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        activeProfiles = new String[0];

        when(mockEnvironment.getActiveProfiles()).thenAnswer(invocationOnMock -> activeProfiles);
        when(mockApplicationContext.getEnvironment()).thenReturn(mockEnvironment);

        ContextClassLoaderApplicationContextHolder.bind(mockApplicationContext);

        strategy = new SpringProfileActivationStrategy();
    }

    @AfterEach
    void tearDown() {
        ContextClassLoaderApplicationContextHolder.release();
    }

    @Test
    void testGetId() {
        assertEquals(SpringProfileActivationStrategy.ID, strategy.getId());
    }

    @Test
    void testGetName() {
        assertTrue(Strings.isNotBlank(strategy.getName()));
    }

    @ParameterizedTest
    @MethodSource("activeTestCases")
    void testIsActive(IsActiveTestCase testCase) {
        activeProfiles = testCase.activeProfiles;

        testCase.run(strategy);
    }

    @Test
    void testIsActiveThrowsWhenNoApplicationContext() {
        FeatureState featureState = new FeatureState(TestFeatures.FEATURE_ONE, true);

        ContextClassLoaderApplicationContextHolder.release();

        assertThrows(IllegalStateException.class, () -> strategy.isActive(featureState, null));
    }

    @Test
    void testGetParameters() {
        Parameter[] parameters = strategy.getParameters();

        assertEquals(1, parameters.length);

        Parameter parameter = parameters[0];

        assertNotNull(parameter);
        assertEquals(SpringProfileActivationStrategy.PARAM_PROFILES, parameter.getName());
        assertTrue(Strings.isNotBlank(parameter.getLabel()));
        assertTrue(Strings.isNotBlank(parameter.getDescription()));
    }

    @Test
    void testGetTokenParameterName() {
        assertEquals(SpringProfileActivationStrategy.PARAM_PROFILES, strategy.getTokenParameterName());
    }

    @Test
    void testGetTokenParameterTransformer() {
        TokenTransformer transformer = strategy.getTokenParameterTransformer();

        assertNotNull(transformer);
        assertEquals("foo", transformer.transform("FOO"));
    }

    enum TestFeatures implements Feature {

        FEATURE_ONE
    }

    private static class IsActiveTestCase {

        private final String[] activeProfiles;
        private final boolean expected;
        private final String message;
        private final String profilesParam;

        IsActiveTestCase(String message, boolean expected, String profilesParam, String... activeProfiles) {
            this.message = (expected ? "Active" : "Inactive") + " when " + message;
            this.expected = expected;
            this.profilesParam = profilesParam;
            this.activeProfiles = activeProfiles == null ? new String[0] : activeProfiles;
        }

        void run(SpringProfileActivationStrategy strategy) {
            FeatureState featureState = new FeatureState(TestFeatures.FEATURE_ONE, !expected);
            featureState.setParameter(SpringProfileActivationStrategy.PARAM_PROFILES, profilesParam);

            assertEquals(expected, strategy.isActive(featureState, null), message);
        }
    }
}
