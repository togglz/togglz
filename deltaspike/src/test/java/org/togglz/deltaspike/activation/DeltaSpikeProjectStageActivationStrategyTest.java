package org.togglz.deltaspike.activation;

import static org.junit.Assert.*;

import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.util.ProjectStageProducer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.togglz.core.Feature;
import org.togglz.core.activation.Parameter;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.util.Strings;

/**
 * <p>
 * Tests for the {@link DeltaSpikeProjectStageActivationStrategy} class.
 * </p>
 *
 * @author Alasdair Mercer
 */
@RunWith(Theories.class)
public class DeltaSpikeProjectStageActivationStrategyTest {

    @DataPoint
    public static final IsActiveTestCase TC_1 = new IsActiveTestCase("stage is not active", false, "Development",
        ProjectStage.Production);
    @DataPoint
    public static final IsActiveTestCase TC_2 = new IsActiveTestCase("stage is not active but param is negated", true,
        "!Development", ProjectStage.Production);
    @DataPoint
    public static final IsActiveTestCase TC_3 = new IsActiveTestCase("stage is active", true, "Development",
        ProjectStage.Development);
    @DataPoint
    public static final IsActiveTestCase TC_4 = new IsActiveTestCase("stage is active and param case is different",
        false, "development", ProjectStage.Development);
    @DataPoint
    public static final IsActiveTestCase TC_5 = new IsActiveTestCase("stage is active but param is negated", false,
        "!Development", ProjectStage.Development);
    @DataPoint
    public static final IsActiveTestCase TC_6 = new IsActiveTestCase("none of stages are active", false,
        "Development,SystemTest,IntegrationTest", ProjectStage.Production);
    @DataPoint
    public static final IsActiveTestCase TC_7 = new IsActiveTestCase(
        "none of stages are active but at least one param is negated", true, "Development,SystemTest,!IntegrationTest",
        ProjectStage.Production);
    @DataPoint
    public static final IsActiveTestCase TC_8 = new IsActiveTestCase("at least one stage is active", true,
        "Development,SystemTest,IntegrationTest", ProjectStage.IntegrationTest);
    @DataPoint
    public static final IsActiveTestCase TC_9 = new IsActiveTestCase(
        "none of stages are active but all params are negated", true, "!Development,!SystemTest,!IntegrationTest",
        ProjectStage.Production);

    private DeltaSpikeProjectStageActivationStrategy strategy;

    @Before
    public void setUp() {
        strategy = new DeltaSpikeProjectStageActivationStrategy();
    }

    @After
    public void tearDown() {
        ProjectStageProducer.setProjectStage(null);
    }

    @Test
    public void testGetId() {
        assertEquals(DeltaSpikeProjectStageActivationStrategy.ID, strategy.getId());
    }

    @Test
    public void testGetName() {
        assertTrue(Strings.isNotBlank(strategy.getName()));
    }

    @Theory
    public void testIsActive(IsActiveTestCase testCase) {
        testCase.run(strategy);
    }

    @Test
    public void testGetParameters() {
        Parameter[] parameters = strategy.getParameters();

        assertEquals(1, parameters.length);

        Parameter parameter = parameters[0];

        assertNotNull(parameter);
        assertEquals(DeltaSpikeProjectStageActivationStrategy.PARAM_STAGES, parameter.getName());
        assertTrue(Strings.isNotBlank(parameter.getLabel()));
        assertTrue(Strings.isNotBlank(parameter.getDescription()));
    }

    @Test
    public void testGetTokenParameterName() {
        assertEquals(DeltaSpikeProjectStageActivationStrategy.PARAM_STAGES, strategy.getTokenParameterName());
    }

    @Test
    public void testGetTokenParameterTransformer() {
        assertNull(strategy.getTokenParameterTransformer());
    }

    public enum TestFeatures implements Feature {

        FEATURE_ONE
    }

    private static class IsActiveTestCase {

        private ProjectStage activeProjectStage;
        private boolean expected;
        private String message;
        private String stagesParam;

        IsActiveTestCase(String message, boolean expected, String stagesParam, ProjectStage activeProjectStage) {
            this.message = (expected ? "Active" : "Inactive") + " when " + message;
            this.expected = expected;
            this.stagesParam = stagesParam;
            this.activeProjectStage = activeProjectStage;
        }

        void run(DeltaSpikeProjectStageActivationStrategy strategy) {
            ProjectStageProducer.setProjectStage(activeProjectStage);

            FeatureState featureState = new FeatureState(TestFeatures.FEATURE_ONE, !expected);
            featureState.setParameter(DeltaSpikeProjectStageActivationStrategy.PARAM_STAGES, stagesParam);

            assertEquals(message, expected, strategy.isActive(featureState, null));
        }
    }
}
