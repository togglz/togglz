package org.togglz.jsp;

import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;

public enum JspTaglibFeature implements Feature {

    ACTIVE_FEATURE,
    INACTIVE_FEATURE;

    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }

}
