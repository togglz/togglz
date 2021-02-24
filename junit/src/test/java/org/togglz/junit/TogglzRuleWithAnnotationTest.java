package org.togglz.junit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;

public class TogglzRuleWithAnnotationTest {

    @Rule
    public TogglzRule togglzRule = TogglzRule.allDisabled(MyFeatures.class);

    @Test
    public void featureShouldBeInactiveByDefault()
    {
        assertFalse(MyFeatures.ONE.isActive());
        assertFalse(MyFeatures.TWO.isActive());
    }

    @Test
    @WithFeature("ONE")
    public void featureShouldBeActiveWithAnnotation()
    {
        assertTrue(MyFeatures.ONE.isActive());
        assertFalse(MyFeatures.TWO.isActive());
    }

    @Test
    @WithFeature({ "ONE", "TWO" })
    public void shouldActivateMultipleFeatures()
    {
        assertTrue(MyFeatures.ONE.isActive());
        assertTrue(MyFeatures.TWO.isActive());
    }

    private enum MyFeatures implements Feature {

        ONE,
        TWO;

        public boolean isActive() {
            return FeatureContext.getFeatureManager().isActive(this);
        }

    }

}
