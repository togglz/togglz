package org.togglz

import org.togglz.core.Feature
import org.togglz.core.repository.FeatureState

enum FeatureFixture implements Feature {

    F1, F2;

    static final FeatureState ENABLE_F1 = new FeatureState(F1).enable();
    static final FeatureState DISABLE_F1 = new FeatureState(F1).disable();
}