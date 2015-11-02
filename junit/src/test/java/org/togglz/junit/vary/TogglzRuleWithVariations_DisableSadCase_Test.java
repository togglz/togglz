package org.togglz.junit.vary;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.togglz.junit.TogglzRule;

import static org.junit.Assert.assertTrue;

public class TogglzRuleWithVariations_DisableSadCase_Test {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public TogglzRule togglzRule = TogglzRule.builder(MyFeatures.class)
            .disable(MyFeatures.F1)
            .build();

    @Test
    public void test() {
        expectedException.expect(AssertionError.class);

        assertTrue(MyFeatures.F1.isActive());
    }


}
