package org.togglz.spring.test.proxy;

import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;

public enum ProxyFeatures implements Feature {

    SERVICE_TOGGLE;

    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }

}
