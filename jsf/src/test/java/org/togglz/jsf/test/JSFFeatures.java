package org.togglz.jsf.test;

import org.togglz.core.Feature;
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.context.FeatureContext;

public enum JSFFeatures implements Feature {

    DISABLED,
    
    @EnabledByDefault
    ENABLED;

    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }

}
