package de.chkal.togglz.test.basic;

import de.chkal.togglz.core.Feature;
import de.chkal.togglz.core.annotation.EnabledByDefault;
import de.chkal.togglz.core.context.FeatureContext;

public enum BasicFeatures implements Feature {

    FEATURE1,
    
    @EnabledByDefault
    FEATURE2;

    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this, null);
    }

}
