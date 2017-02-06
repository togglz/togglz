package org.togglz

import org.togglz.core.Feature
import org.togglz.core.annotation.Label
import org.togglz.core.repository.FeatureState

enum FeatureFixture implements Feature {

    F1,
    @Label("label2")F2;

    public static final FeatureState ENABLE_F1 = new FeatureState(F1).enable();
    public static final FeatureState DISABLE_F1 = new FeatureState(F1).disable();
    public static final FeatureState ENABLE_F2 = new FeatureState(F2).enable();
}