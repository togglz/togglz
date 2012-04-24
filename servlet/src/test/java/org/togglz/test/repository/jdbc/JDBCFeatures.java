package org.togglz.test.repository.jdbc;

import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;

public enum JDBCFeatures implements Feature {

    F1, F2;

    @Override
    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }

}
