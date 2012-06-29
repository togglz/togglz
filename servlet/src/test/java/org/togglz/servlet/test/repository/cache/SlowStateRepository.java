package org.togglz.servlet.test.repository.cache;

import java.util.HashMap;
import java.util.Map;

import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

public class SlowStateRepository implements StateRepository {

    private final Map<Feature, FeatureState> states = new HashMap<Feature, FeatureState>();

    public FeatureState getFeatureState(Feature feature) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
        return states.get(feature);
    }

    public void setFeatureState(FeatureState featureState) {
        states.put(featureState.getFeature(), featureState);
    }

}
