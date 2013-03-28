package org.togglz.junit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;

public class TogglzRuleWithAnnotationTest {

    @Rule
    public TogglzRule togglzRule = TogglzRule.allDisabled(MyFeatures.class);

    @Test
    public void featureShouldBeInactiveByDefault()
    {
        assertFalse(MyFeatures.FEATURE_ONE.isActive());
    }

    @Test
    @WithFeature("FEATURE_ONE")
    public void featureShouldBeActiveWithAnnotation()
    {
        assertTrue(MyFeatures.FEATURE_ONE.isActive());
    }

}
