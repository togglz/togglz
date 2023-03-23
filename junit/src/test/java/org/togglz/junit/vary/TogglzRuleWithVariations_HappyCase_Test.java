package org.togglz.junit.vary;

import org.junit.Rule;
import org.junit.Test;
import org.togglz.junit.TogglzRule;

import static org.junit.Assert.assertTrue;

public class TogglzRuleWithVariations_HappyCase_Test {

    @Rule
    public TogglzRule togglzRule = TogglzRule.builder(MyFeatures.class)
            .enable(MyFeatures.F1)
            .vary(MyFeatures.F2)
            .vary(MyFeatures.F3)
            .build();

    @Test
    public void test() {
        assertTrue(MyFeatures.F1.isActive());
        assertTrue(MyFeatures.F2.isActive() || !MyFeatures.F2.isActive());
        assertTrue(MyFeatures.F3.isActive() || !MyFeatures.F3.isActive());
    }

}
