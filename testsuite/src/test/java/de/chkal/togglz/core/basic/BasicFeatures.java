package de.chkal.togglz.core.basic;

import de.chkal.togglz.core.Feature;
import de.chkal.togglz.core.holder.FeatureManagerHolder;

public enum BasicFeatures implements Feature {

    FEATURE1,
    FEATURE2;

    public boolean isActive() {
        return FeatureManagerHolder.getFeatureManager().isActive(this, null);
    }

}
