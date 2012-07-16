package org.togglz.junit;

import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;

enum MyFeatures implements Feature {

    FEATURE_ONE;

    @Override
    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }

}