package org.togglz.core.repository.mem;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;

public class InMemoryStateRepositoryTest {

    private InMemoryStateRepository repository;

    @Before
    public void before() throws SQLException {
        repository = new InMemoryStateRepository();
    }

    @Test
    public void testGetFeatureStateNotSameAsSetFeatureState() {
        FeatureState featureState = createDisabledFeatureState();
        repository.setFeatureState(featureState);
        FeatureState featureStateFromRepo = repository.getFeatureState(MyFeature.FEATURE1);
        assertThat(featureStateFromRepo).isNotSameAs(featureState);
    }

    @Test
    public void testGetFeatureStateChangeNotAffectsInternalRepositoryState() {
        repository.setFeatureState(createDisabledFeatureState());
        FeatureState featureStateFromRepo = repository.getFeatureState(MyFeature.FEATURE1);
        assertThat(featureStateFromRepo.isEnabled()).isFalse();
        // change feature state but not "persist" it (we don't call repository.setFeatureState)
        featureStateFromRepo.setEnabled(true);
        // obtain persisted feature again
        featureStateFromRepo = repository.getFeatureState(MyFeature.FEATURE1);
        assertThat(featureStateFromRepo.isEnabled()).isFalse();
    }

    @Test
    public void testSetFeatureStateChangeNotAffectsInternalRepositoryState() {
        FeatureState featureState = createDisabledFeatureState();
        repository.setFeatureState(featureState);
        // change feature state after "persisting" it
        featureState.setEnabled(true);
        // obtain persisted feature
        FeatureState featureStateFromRepo = repository.getFeatureState(MyFeature.FEATURE1);
        assertThat(featureStateFromRepo.isEnabled()).isFalse();
    }

    protected FeatureState createDisabledFeatureState() {
        FeatureState featureState = new FeatureState(MyFeature.FEATURE1);
        featureState.setEnabled(false);
        return featureState;
    }

    private static enum MyFeature implements Feature {
        FEATURE1,
        FEATURE2
    }
}
