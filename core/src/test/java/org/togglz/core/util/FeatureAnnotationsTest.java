package org.togglz.core.util;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.annotation.Label;

public class FeatureAnnotationsTest {

    @Test
    public void testGetLabel() {

        assertEquals("Some feature with a label", FeatureAnnotations.getLabel(MyFeature.FEATURE_WITH_LABEL));
        assertEquals("FEATURE_WITHOUT_LABEL", FeatureAnnotations.getLabel(MyFeature.FEATURE_WITHOUT_LABEL));

    }

    @Test
    public void testIsEnabledByDefault() {

        assertEquals(false, FeatureAnnotations.isEnabledByDefault(MyFeature.FEATURE_WITH_LABEL));
        assertEquals(false, FeatureAnnotations.isEnabledByDefault(MyFeature.FEATURE_WITHOUT_LABEL));
        assertEquals(true, FeatureAnnotations.isEnabledByDefault(MyFeature.FEATURE_ENABLED_BY_DEFAULT));

    }

    private static enum MyFeature implements Feature {

        @Label("Some feature with a label")
        FEATURE_WITH_LABEL,

        // no label annotation
        FEATURE_WITHOUT_LABEL,

        @EnabledByDefault
        FEATURE_ENABLED_BY_DEFAULT;

    }

}
