package org.togglz.cdi;

import org.togglz.core.Feature;
import org.togglz.core.annotation.EnabledByDefault;

public enum Features implements Feature {

    FEATURE1,

    @EnabledByDefault
    FEATURE2

}
