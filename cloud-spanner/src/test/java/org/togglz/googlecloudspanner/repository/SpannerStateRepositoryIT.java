package org.togglz.googlecloudspanner.repository;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;

import static org.junit.jupiter.api.Assertions.*;

public class SpannerStateRepositoryIT {

    private static final Feature TEST_FEATURE = TestFeature.F1;
    private static final String STRATEGY_ID = "myStrategy";
    private static final String STRATEGY_PARAM_NAME = "foo";
    private static final String STRATEGY_PARAM_VALUE = "bar";

    private static final SpannerEmulatorControl spannerEmulatorControl = new SpannerEmulatorControl();

    @BeforeAll
    static void before() {
        spannerEmulatorControl.start();
    }

    @AfterAll
    static void after() {
        spannerEmulatorControl.stop();
    }

    private SpannerStateRepository repository;

    @BeforeEach
    public void setUp() {
        this.repository = new SpannerStateRepository(spannerEmulatorControl.getDatabaseClient());
    }

    @Test
    public void shouldReturnNullWhenStateDoesntExist() {
        givenIsNoPersistentFeatureState();

        thenNoFeatureStateFound();
    }

    @Test
    public void shouldSaveStateWithoutStrategyOrParameters() {
        givenIsNoPersistentFeatureState();

        whenNewFeatureStateSetToDisabled();

        thenPersistedFeatureStateIsDisabled();
    }

    @Test
    public void shouldSaveStateWithStrategyAndParameters() {
        givenIsNoPersistentFeatureState();

        whenFeatureStateSavedWithDisabledWithStrageyIdAndWithStrategyParams();

        thenPersistedFeatureStateIsDisabledWithStrategyIdAndParams();
    }

    @Test
    public void shouldUpdateExistingState() {
        givenPersistentFeatureStateIsEnabledWithStrategyIdAndParams();

        whenFeatureStateSetToDisabled();

        thenPersistedFeatureStateIsDisabledWithStrategyIdAndParams();
    }

    private void thenPersistedFeatureStateIsDisabledWithStrategyIdAndParams() {
        FeatureState featureState = repository.getFeatureState(TEST_FEATURE);
        assertNotNull(featureState);
        assertFalse(featureState.isEnabled());
        assertEquals(STRATEGY_ID, featureState.getStrategyId());
        assertEquals(STRATEGY_PARAM_VALUE, featureState.getParameter(STRATEGY_PARAM_NAME));
    }

    private void givenPersistentFeatureStateIsEnabledWithStrategyIdAndParams() {
        final FeatureState featureState = new FeatureState(TEST_FEATURE)
                .enable()
                .setStrategyId(STRATEGY_ID)
                .setParameter(STRATEGY_PARAM_NAME, STRATEGY_PARAM_VALUE);
        repository.setFeatureState(featureState);
    }


    private void whenFeatureStateSavedWithDisabledWithStrageyIdAndWithStrategyParams() {
        final FeatureState featureState = new FeatureState(TEST_FEATURE)
                .disable()
                .setStrategyId(STRATEGY_ID)
                .setParameter(STRATEGY_PARAM_NAME, STRATEGY_PARAM_VALUE);
        repository.setFeatureState(featureState);
    }

    private void thenPersistedFeatureStateIsDisabled() {
        FeatureState featureState = repository.getFeatureState(TEST_FEATURE);

        assertNotNull(featureState);
        assertEquals(TEST_FEATURE, featureState.getFeature());
        assertFalse(featureState.isEnabled());
    }

    private void whenNewFeatureStateSetToDisabled() {
        FeatureState featureState = new FeatureState(TEST_FEATURE);
        featureState.disable();
        repository.setFeatureState(featureState);
    }

    private void whenFeatureStateSetToDisabled() {
        FeatureState featureState = repository.getFeatureState(TEST_FEATURE);
        featureState.disable();
        repository.setFeatureState(featureState);
    }

    private void givenIsNoPersistentFeatureState() {
        repository.removeFeatureState(TEST_FEATURE);
        assertFalse(repository.existsFeatureState(TEST_FEATURE));
    }

    private void thenNoFeatureStateFound() {
        FeatureState result = repository.getFeatureState(TEST_FEATURE);
        assertNull(result);
    }

    private enum TestFeature implements Feature {
        F1
    }
}
