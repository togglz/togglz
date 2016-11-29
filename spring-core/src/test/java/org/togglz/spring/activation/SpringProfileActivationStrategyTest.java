package org.togglz.spring.activation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.togglz.core.Feature;
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
@RunWith(MockitoJUnitRunner.class)
public class SpringProfileActivationStrategyTest {

    private static final String PROFILE_1 = "profile1";
    private static final String PROFILE_2 = "profile2";
    private static final String PROFILE_3 = "profile3";

    @Mock
    private ApplicationContext mockApplicationContext;
    @Mock
    private Environment mockEnvironment;

    private SpringProfileActivationStrategy strategy;

    @Before
    public void setUp() {
        when(mockApplicationContext.getEnvironment()).thenReturn(mockEnvironment);

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

    @Test
    public void testIsActiveWithMultipleProfilesWhenActive() {
        FeatureState featureState = new FeatureState(TestFeatures.FEATURE_ONE, false);
        featureState.setParameter(SpringProfileActivationStrategy.PARAM_PROFILES, PROFILE_2);

        ContextClassLoaderApplicationContextHolder.bind(mockApplicationContext);

        when(mockEnvironment.getActiveProfiles()).thenReturn(new String[]{PROFILE_1, PROFILE_2});

        assertTrue(strategy.isActive(featureState, null));
    }

    @Test
    public void testIsActiveWithMultipleProfilesWhenInactive() {
        FeatureState featureState = new FeatureState(TestFeatures.FEATURE_ONE, true);
        featureState.setParameter(SpringProfileActivationStrategy.PARAM_PROFILES, PROFILE_3);

        ContextClassLoaderApplicationContextHolder.bind(mockApplicationContext);

        when(mockEnvironment.getActiveProfiles()).thenReturn(new String[]{PROFILE_1, PROFILE_2});

        assertFalse(strategy.isActive(featureState, null));
    }

    @Test
    public void testIsActiveWithNoProfiles() {
        FeatureState featureState = new FeatureState(TestFeatures.FEATURE_ONE, true);
        featureState.setParameter(SpringProfileActivationStrategy.PARAM_PROFILES, PROFILE_1);

        ContextClassLoaderApplicationContextHolder.bind(mockApplicationContext);

        when(mockEnvironment.getActiveProfiles()).thenReturn(new String[0]);

        assertFalse(strategy.isActive(featureState, null));
    }

    @Test
    public void testIsActiveWithSingleProfileWhenActive() {
        FeatureState featureState = new FeatureState(TestFeatures.FEATURE_ONE, false);
        featureState.setParameter(SpringProfileActivationStrategy.PARAM_PROFILES, PROFILE_1);

        ContextClassLoaderApplicationContextHolder.bind(mockApplicationContext);

        when(mockEnvironment.getActiveProfiles()).thenReturn(new String[]{PROFILE_1});

        assertTrue(strategy.isActive(featureState, null));
    }

    @Test
    public void testIsActiveWithSingleProfileWhenInactive() {
        FeatureState featureState = new FeatureState(TestFeatures.FEATURE_ONE, false);
        featureState.setParameter(SpringProfileActivationStrategy.PARAM_PROFILES, PROFILE_2);

        ContextClassLoaderApplicationContextHolder.bind(mockApplicationContext);

        when(mockEnvironment.getActiveProfiles()).thenReturn(new String[]{PROFILE_1});

        assertFalse(strategy.isActive(featureState, null));
    }

    @Test
    public void testIsActiveWithNoApplicationContext() {
        FeatureState featureState = new FeatureState(TestFeatures.FEATURE_ONE, true);

        assertFalse(strategy.isActive(featureState, null));
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

    public enum TestFeatures implements Feature {

        FEATURE_ONE
    }
}
