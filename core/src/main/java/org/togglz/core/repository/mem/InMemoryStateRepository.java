package org.togglz.core.repository.mem;

import java.util.HashMap;
import java.util.Map;

import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;


/**
 * 
 * A very simply implementation of {@link StateRepository} entirely on memory. This class is typically only used for
 * integration tests or at development time.
 * 
 * @author Christian Kaltepoth
 * 
 */
public class InMemoryStateRepository implements StateRepository {

    private final Map<Feature, FeatureState> states = new HashMap<Feature, FeatureState>();

    public FeatureState getFeatureState(Feature feature) {
        return states.get(feature);
    }

    public void setFeatureState(FeatureState featureState) {
        states.put(featureState.getFeature(), featureState);
    }

}
