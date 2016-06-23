package org.togglz.zookeeper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ddcbdevins on 5/27/16.
 */
public class FeatureStateStorageWrapper {

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
}
