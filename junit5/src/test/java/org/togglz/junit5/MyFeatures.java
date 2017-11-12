package org.togglz.junit5;

import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;

/**
 * @author Roland Weisleder
 */
enum MyFeatures implements Feature {

    ONE, TWO, THREE;

    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }
}
