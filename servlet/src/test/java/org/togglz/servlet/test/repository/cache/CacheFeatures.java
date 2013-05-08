package org.togglz.servlet.test.repository.cache;

import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;

public enum CacheFeatures implements Feature {

    F1, F2;

    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }

}
