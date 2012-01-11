package de.chkal.togglz.test.jsf;

import de.chkal.togglz.core.Feature;
import de.chkal.togglz.core.annotation.EnabledByDefault;
import de.chkal.togglz.core.context.FeatureContext;

public enum JSFFeatures implements Feature {

    DISABLED,
    
    @EnabledByDefault
    ENABLED;

    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }

}
