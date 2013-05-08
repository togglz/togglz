package org.togglz.servlet.test.repository.jdbc;

import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;

public enum JDBCFeatures implements Feature {

    F1, F2;

    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }

}
