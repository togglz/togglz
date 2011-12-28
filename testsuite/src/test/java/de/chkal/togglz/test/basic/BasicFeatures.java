package de.chkal.togglz.test.basic;

import de.chkal.togglz.core.Feature;
import de.chkal.togglz.core.annotation.EnabledByDefault;
import de.chkal.togglz.core.holder.FeatureManagerHolder;

public enum BasicFeatures implements Feature {

    FEATURE1,
    
    @EnabledByDefault
    FEATURE2;

    public boolean isActive() {
        return FeatureManagerHolder.getFeatureManager().isActive(this, null);
    }

}
