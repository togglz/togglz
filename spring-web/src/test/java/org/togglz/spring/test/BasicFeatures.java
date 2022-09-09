package org.togglz.spring.test;

import org.togglz.core.Feature;
import org.togglz.core.annotation.EnabledByDefault;

public enum BasicFeatures implements Feature {

    FEATURE1,

    @EnabledByDefault
    FEATURE2

}
