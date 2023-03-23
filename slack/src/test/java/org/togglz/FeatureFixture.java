package org.togglz;

import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;
import org.togglz.core.repository.FeatureState;

public enum FeatureFixture implements Feature {

    F1,
    @Label("label2")F2;

    public static final FeatureState ENABLE_F1 = new FeatureState(FeatureFixture.F1).enable();
    public static final FeatureState DISABLE_F1 = new FeatureState(FeatureFixture.F1).disable();
    public static final FeatureState ENABLE_F2 = new FeatureState(FeatureFixture.F2).enable();
}
