package org.togglz.core.util;

import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Wraps the state of a feature (minus the feature itself) to make it easier to store
 * the state of a feature in a cache and then recreate the FeatureState later.
 *
 * This can help assist in the creation of StateRepositories in some circumstances
 *
 * Created by ddcbdevins on 5/27/16.
 */
public class FeatureStateStorageWrapper implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean enabled = false;
    private String strategyId;
    private final Map<String, String> parameters = new HashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(String strategyId) {
        this.strategyId = strategyId;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public static FeatureStateStorageWrapper wrapperForFeatureState(FeatureState featureState) {
        FeatureStateStorageWrapper wrapper = new FeatureStateStorageWrapper();
        wrapper.setEnabled(featureState.isEnabled());
        wrapper.setStrategyId(featureState.getStrategyId());
        wrapper.getParameters().putAll(featureState.getParameterMap());

        return wrapper;
    }

    public static FeatureState featureStateForWrapper(Feature feature, FeatureStateStorageWrapper wrapper) {
        FeatureState featureState = new FeatureState(feature);
        featureState.setEnabled(wrapper.isEnabled());
        featureState.setStrategyId(wrapper.getStrategyId());
        for (Map.Entry<String, String> e : wrapper.getParameters().entrySet()) {
            featureState.setParameter(e.getKey(), e.getValue());
        }

        return featureState;
    }

}
