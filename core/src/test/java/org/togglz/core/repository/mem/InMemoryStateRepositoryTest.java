package org.togglz.core.repository.mem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryStateRepositoryTest {

    private InMemoryStateRepository repository;

    @BeforeEach
    void before() {
        repository = new InMemoryStateRepository();
    }

    @Test
    void testGetFeatureStateNotSameAsSetFeatureState() {
        FeatureState featureState = createDisabledFeatureState();
        repository.setFeatureState(featureState);
        FeatureState featureStateFromRepo = repository.getFeatureState(MyFeature.FEATURE1);
        assertNotEquals(featureState, featureStateFromRepo);
    }

    @Test
    void testGetFeatureStateChangeNotAffectsInternalRepositoryState() {
        repository.setFeatureState(createDisabledFeatureState());
        FeatureState featureStateFromRepo = repository.getFeatureState(MyFeature.FEATURE1);
        assertFalse(featureStateFromRepo.isEnabled());
        // change feature state but not "persist" it (we don't call repository.setFeatureState)
        featureStateFromRepo.setEnabled(true);
        // obtain persisted feature again
        featureStateFromRepo = repository.getFeatureState(MyFeature.FEATURE1);
        assertFalse(featureStateFromRepo.isEnabled());
    }

    @Test
    void testSetFeatureStateChangeNotAffectsInternalRepositoryState() {
        FeatureState featureState = createDisabledFeatureState();
        repository.setFeatureState(featureState);
        // change feature state after "persisting" it
        featureState.setEnabled(true);
        // obtain persisted feature
        FeatureState featureStateFromRepo = repository.getFeatureState(MyFeature.FEATURE1);
        assertFalse(featureStateFromRepo.isEnabled());
    }

    protected FeatureState createDisabledFeatureState() {
        FeatureState featureState = new FeatureState(MyFeature.FEATURE1);
        featureState.setEnabled(false);
        return featureState;
    }

    private enum MyFeature implements Feature {
        FEATURE1,
        FEATURE2
    }
}
