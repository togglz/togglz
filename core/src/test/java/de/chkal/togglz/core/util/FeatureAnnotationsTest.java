package de.chkal.togglz.core.util;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;

import de.chkal.togglz.core.Feature;
import de.chkal.togglz.core.annotation.EnabledByDefault;
import de.chkal.togglz.core.annotation.Label;

public class FeatureAnnotationsTest {

    @Test
    public void testGetLabel() {
        
        assertEquals("Some feature with a label", MyFeature.FEATURE_WITH_LABEL.label());
        assertEquals("FEATURE_WITHOUT_LABEL", MyFeature.FEATURE_WITHOUT_LABEL.label());
        
    }
    
    @Test
    public void testIsEnabledByDefault() {
        
        assertEquals(false, MyFeature.FEATURE_WITH_LABEL.enabledByDefault());
        assertEquals(false, MyFeature.FEATURE_WITHOUT_LABEL.enabledByDefault());
        assertEquals(true, MyFeature.FEATURE_ENABLED_BY_DEFAULT.enabledByDefault());
        
    }
    
    private static enum MyFeature implements Feature {

        @Label("Some feature with a label")
        FEATURE_WITH_LABEL,
        
        // no label annotation
        FEATURE_WITHOUT_LABEL,

        @EnabledByDefault
        FEATURE_ENABLED_BY_DEFAULT;
        
        @Override
        public String label() {
            return FeatureAnnotations.getLabel(this);
        }

        @Override
        public boolean enabledByDefault() {
            return FeatureAnnotations.isEnabledByDefault(this);
        }
        
    }

}
