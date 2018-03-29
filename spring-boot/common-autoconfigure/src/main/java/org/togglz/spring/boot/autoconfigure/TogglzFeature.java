package org.togglz.spring.boot.autoconfigure;

import java.util.Map;

import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;

public class TogglzFeature implements Comparable<TogglzFeature> {

    private String name;
    private boolean enabled;
    private String strategy;
    private Map<String, String> params;

    public TogglzFeature(Feature feature, FeatureState featureState) {
        this.name = feature.name();
        this.enabled = featureState.isEnabled();
        this.strategy = featureState.getStrategyId();
        this.params = featureState.getParameterMap();
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getStrategy() {
        return strategy;
    }

    public Map<String, String> getParams() {
        return params;
    }

	@Override
	public int compareTo(TogglzFeature o) {
		return name.compareTo(o.getName());
	}
}