package org.togglz.junit.vary;

import org.junit.Rule;
import org.junit.Test;
import org.togglz.junit.TogglzRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TogglzRuleWithVariations_CalledMultipleTimes_Test {

    @Rule
    public TogglzRule togglzRule = TogglzRule.builder(MyFeatures.class)
        .vary(MyFeatures.F2)
        .vary(MyFeatures.F3)
        .build();

    private int counter;

    @Test
    public void test() {

        counter++;

        switch (counter) {
            case 1:
                assertEquals("C1, F1", false, MyFeatures.F1.isActive());
                assertEquals("C1, F2", false, MyFeatures.F2.isActive());
                assertEquals("C1, F3", false, MyFeatures.F3.isActive());
                break;

            case 2:
                assertEquals("C2, F1", false, MyFeatures.F1.isActive());
                assertEquals("C2, F2", true, MyFeatures.F2.isActive());
                assertEquals("C2, F3", false, MyFeatures.F3.isActive());
                break;
            case 3:
                assertEquals("C3, F1", false, MyFeatures.F1.isActive());
                assertEquals("C3, F2", false, MyFeatures.F2.isActive());
                assertEquals("C3, F3", true, MyFeatures.F3.isActive());
                break;

            case 4:
                assertEquals("C4, F1", false, MyFeatures.F1.isActive());
                assertEquals("C4, F2", true, MyFeatures.F2.isActive());
                assertEquals("C4, F3", true, MyFeatures.F3.isActive());
                break;

            default:
                fail("Incorrect execution cound");

        }
    }

}
