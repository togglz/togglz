package de.chkal.togglz.core.repository.mem;

import java.util.HashMap;
import java.util.Map;

import de.chkal.togglz.core.Feature;
import de.chkal.togglz.core.manager.FeatureState;
import de.chkal.togglz.core.repository.FeatureStateRepository;

public class InMemoryRepository implements FeatureStateRepository {

    private final Map<Feature, FeatureState> states = new HashMap<Feature, FeatureState>();

    public FeatureState getFeatureState(Feature feature) {
        FeatureState featureState = states.get(feature);
        if (featureState == null) {
            featureState = new FeatureState(feature, false);
        }
        return featureState;
    }

    public void setFeatureState(FeatureState featureState) {
        states.put(featureState.getFeature(), featureState);

    }

}
