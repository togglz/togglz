package org.togglz.spring.activation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.togglz.core.Feature;
import org.togglz.core.activation.AbstractTokenizedActivationStrategy.TokenTransformer;
import org.togglz.core.activation.Parameter;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.util.Strings;
import org.togglz.spring.util.ContextClassLoaderApplicationContextHolder;

/**
 * <p>
 * Tests for the {@link SpringProfileActivationStrategy} class.
 * </p>
 *
 * @author Alasdair Mercer
 */
@RunWith(Theories.class)
public class SpringProfileActivationStrategyTest {

    @DataPoint
    public static final IsActiveTestCase TC_1 = new IsActiveTestCase("profile is not active", false, "p1", "p2");
    @DataPoint
    public static final IsActiveTestCase TC_2 = new IsActiveTestCase("profile is not active but param is negated", true,
        "!p1", "p2");
    @DataPoint
    public static final IsActiveTestCase TC_3 = new IsActiveTestCase("profile is active", true, "p1", "p1");
    @DataPoint
    public static final IsActiveTestCase TC_4 = new IsActiveTestCase("profile is active and param case is different",
        true, "p1", "P1");
    @DataPoint
    public static final IsActiveTestCase TC_5 = new IsActiveTestCase(
        "profile is active and param case is different (inverted)", true, "P1", "p1");
    @DataPoint
    public static final IsActiveTestCase TC_6 = new IsActiveTestCase("profile is active but param is negated", false,
        "!p1", "p1");
    @DataPoint
    public static final IsActiveTestCase TC_7 = new IsActiveTestCase("none of profiles are active", false, "p1,p2,p3",
        "p4");
    @DataPoint
    public static final IsActiveTestCase TC_8 = new IsActiveTestCase(
        "none of profiles are active but at least one param is negated", true, "p1,p2,!p3", "p4");
    @DataPoint
    public static final IsActiveTestCase TC_9 = new IsActiveTestCase("at least one profile is active", true, "p1,p2,p3",
        "p3", "p4");
    @DataPoint
    public static final IsActiveTestCase TC_10 = new IsActiveTestCase("all profiles are active", true, "p1,p2,p3", "p1",
        "p2", "p3", "p4");
    @DataPoint
    public static final IsActiveTestCase TC_11 = new IsActiveTestCase(
        "none of profiles are active but all params are negated", true, "!p1,!p2,!p3", "p4");
    @DataPoint
    public static final IsActiveTestCase TC_12 = new IsActiveTestCase("no profiles are active", false, "p1");
    @DataPoint
    public static final IsActiveTestCase TC_13 = new IsActiveTestCase("no profiles are active but param is negated",
        true, "!p1");

    private String[] activeProfiles;
    @Mock
    private ApplicationContext mockApplicationContext;
    @Mock
    private Environment mockEnvironment;

    private SpringProfileActivationStrategy strategy;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        activeProfiles = new String[0];

        when(mockEnvironment.getActiveProfiles()).thenAnswer(new Answer<Object>() {
            @Override public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return activeProfiles;
            }
        });
        when(mockApplicationContext.getEnvironment()).thenReturn(mockEnvironment);

        ContextClassLoaderApplicationContextHolder.bind(mockApplicationContext);

        strategy = new SpringProfileActivationStrategy();
    }

    @After
    public void tearDown() {
        ContextClassLoaderApplicationContextHolder.release();
    }

    @Test
    public void testGetId() {
        assertEquals(SpringProfileActivationStrategy.ID, strategy.getId());
    }

    @Test
    public void testGetName() {
        assertTrue(Strings.isNotBlank(strategy.getName()));
    }

    @Theory
    public void testIsActive(IsActiveTestCase testCase) {
        activeProfiles = testCase.activeProfiles;

        testCase.run(strategy);
    }

    @Test(expected = IllegalStateException.class)
    public void testIsActiveThrowsWhenNoApplicationContext() {
        FeatureState featureState = new FeatureState(TestFeatures.FEATURE_ONE, true);

        ContextClassLoaderApplicationContextHolder.release();

        strategy.isActive(featureState, null);
    }

    @Test
    public void testGetParameters() {
        Parameter[] parameters = strategy.getParameters();

        assertEquals(1, parameters.length);

        Parameter parameter = parameters[0];

        assertNotNull(parameter);
        assertEquals(SpringProfileActivationStrategy.PARAM_PROFILES, parameter.getName());
        assertTrue(Strings.isNotBlank(parameter.getLabel()));
        assertTrue(Strings.isNotBlank(parameter.getDescription()));
    }

    @Test
    public void testGetTokenParameterName() {
        assertEquals(SpringProfileActivationStrategy.PARAM_PROFILES, strategy.getTokenParameterName());
    }

    @Test
    public void testGetTokenParameterTransformer() {
        TokenTransformer transformer = strategy.getTokenParameterTransformer();

        assertNotNull(transformer);
        assertEquals("foo", transformer.transform("FOO"));
    }

    public enum TestFeatures implements Feature {

        FEATURE_ONE
    }

    private static class IsActiveTestCase {

        private String[] activeProfiles;
        private boolean expected;
        private String message;
        private String profilesParam;

        IsActiveTestCase(String message, boolean expected, String profilesParam, String... activeProfiles) {
            this.message = (expected ? "Active" : "Inactive") + " when " + message;
            this.expected = expected;
            this.profilesParam = profilesParam;
            this.activeProfiles = activeProfiles == null ? new String[0] : activeProfiles;
        }

        void run(SpringProfileActivationStrategy strategy) {
            FeatureState featureState = new FeatureState(TestFeatures.FEATURE_ONE, !expected);
            featureState.setParameter(SpringProfileActivationStrategy.PARAM_PROFILES, profilesParam);

            assertEquals(message, expected, strategy.isActive(featureState, null));
        }
    }
}
