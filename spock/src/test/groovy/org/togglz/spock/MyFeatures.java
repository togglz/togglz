package org.togglz.spock;

import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;

public enum MyFeatures implements Feature {
    ONE, TWO, THREE;

    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }
}
