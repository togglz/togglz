package org.togglz.junit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;

public class TogglzRuleAllEnabledTest {

    @Rule
    public TogglzRule togglzRule = TogglzRule.allEnabled(MyFeatures.class);

    @Test
    public void testActiveByDefault() {

        // should be true by default
        assertTrue(MyFeatures.FEATURE_ONE.isActive());

        // second result should be the same
        assertTrue(MyFeatures.FEATURE_ONE.isActive());

    }

    @Test
    public void testToggleFeature() {

        // initially true
        assertTrue(MyFeatures.FEATURE_ONE.isActive());

        // disable and check result
        togglzRule.disable(MyFeatures.FEATURE_ONE);
        assertFalse(MyFeatures.FEATURE_ONE.isActive());

        // enable and check result
        togglzRule.enable(MyFeatures.FEATURE_ONE);
        assertTrue(MyFeatures.FEATURE_ONE.isActive());

    }

}
