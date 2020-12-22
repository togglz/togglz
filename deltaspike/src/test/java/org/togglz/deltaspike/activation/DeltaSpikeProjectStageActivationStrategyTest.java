package org.togglz.deltaspike.activation;

import static org.apache.deltaspike.core.api.projectstage.ProjectStage.*;
import static org.junit.jupiter.api.Assertions.*;

import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.util.ProjectStageProducer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.togglz.core.Feature;
import org.togglz.core.activation.Parameter;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.util.Strings;

import java.util.stream.Stream;

/**
 * <p>
 * Tests for the {@link DeltaSpikeProjectStageActivationStrategy} class.
 * </p>
 *
 * @author Alasdair Mercer
 */
public class DeltaSpikeProjectStageActivationStrategyTest {

    private DeltaSpikeProjectStageActivationStrategy strategy;

    @BeforeEach
    public void setUp() {
        strategy = new DeltaSpikeProjectStageActivationStrategy();
    }

    @AfterEach
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

    @ParameterizedTest
    @MethodSource("isActiveTestCaseForStage")
    public void testIsActive(IsActiveTestCase testCase) {
        testCase.run(strategy);
    }

    private static Stream<Arguments> isActiveTestCaseForStage() {
        return Stream.of(
                Arguments.of(new IsActiveTestCase("stage is not active", false, "Development", Production)),
                Arguments.of(new IsActiveTestCase("stage is not active but param is negated", true, "!Development", Production)),
                Arguments.of(new IsActiveTestCase("stage is active", true, "Development", Development)),
                Arguments.of(new IsActiveTestCase("none of stages are active", false, "Development,SystemTest,IntegrationTest", Production)),
                Arguments.of(new IsActiveTestCase(
                        "none of stages are active but at least one param is negated", true, "Development,SystemTest,!IntegrationTest", Production)),
                Arguments.of(new IsActiveTestCase("at least one stage is active", true, "Development,SystemTest,IntegrationTest", IntegrationTest)),
                Arguments.of(new IsActiveTestCase(
                        "none of stages are active but all params are negated", true, "!Development,!SystemTest,!IntegrationTest", Production))
        );
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
        private final ProjectStage activeProjectStage;
        private final boolean expected;
        private final String message;
        private final String stagesParam;

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

            assertEquals(expected, strategy.isActive(featureState, null), message);
        }
    }
}
