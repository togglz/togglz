package org.togglz.guice;

import org.togglz.core.Feature;
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.context.FeatureContext;

public enum GuiceFeatures implements Feature {

    FEATURE1,

    @EnabledByDefault
    FEATURE2;

    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }

}
