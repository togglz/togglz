package org.togglz.testing;

import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;

enum MyFeatures implements Feature {

    FEATURE_ONE;

    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }

}