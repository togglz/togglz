package org.togglz.junit.vary;

import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;

enum MyFeatures implements Feature {

    F1, F2, F3;

    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }

}