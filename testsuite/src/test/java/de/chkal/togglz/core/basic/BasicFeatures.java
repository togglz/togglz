package de.chkal.togglz.core.basic;

import de.chkal.togglz.core.Feature;
import de.chkal.togglz.core.holder.FeatureManagerHolder;

public enum BasicFeatures implements Feature {

    FEATURE1,
    FEATURE2;

    @Override
    public boolean isEnabled() {
        return FeatureManagerHolder.getFeatureManager().isActive(this, null);
    }

    @Override
    public String label() {
        return name();
    }

    @Override
    public boolean enabledByDefault() {
        return false;
    }
    
}
