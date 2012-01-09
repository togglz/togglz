package de.chkal.togglz.test.user.thread;

import de.chkal.togglz.core.Feature;
import de.chkal.togglz.core.context.FeatureContext;

public enum UserDependentFeature implements Feature {

    DISABLED,
    ENABLED_FOR_ALL,
    ENABLED_FOR_CK;

    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }

}
