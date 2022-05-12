package org.togglz.guice;

import org.togglz.core.Feature;
import org.togglz.core.annotation.EnabledByDefault;

public enum GuiceFeatures implements Feature {

    FEATURE1,

    @EnabledByDefault
    FEATURE2

}
