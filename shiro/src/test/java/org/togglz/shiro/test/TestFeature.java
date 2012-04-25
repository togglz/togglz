package org.togglz.shiro.test;

import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;

public enum TestFeature implements Feature {

    DISABLED,
    ENABLED_FOR_ALL,
    ENABLED_FOR_CK;

    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }

}
