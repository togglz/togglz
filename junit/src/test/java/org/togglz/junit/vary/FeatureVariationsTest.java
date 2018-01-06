package org.togglz.junit.vary;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(FeatureVariations.class)
public class FeatureVariationsTest {

    private static int beforeClassCount;

    @Variations
    public static VariationSet<MyFeatures> getPermutations() {
        return VariationSetBuilder.create(MyFeatures.class)
            .enable(MyFeatures.F1)
            .vary(MyFeatures.F2)
            .vary(MyFeatures.F3);
    }

    @BeforeClass
    public static void setUpClass() {
        beforeClassCount++;
    }

    @Test
    public void test() {
        // Test #185 is fixed by ensuring the @BeforeClass method is only called once
        assertEquals(1, beforeClassCount);

        assertTrue(MyFeatures.F1.isActive());
        assertTrue(MyFeatures.F2.isActive() || !MyFeatures.F2.isActive());
        assertTrue(MyFeatures.F3.isActive() || !MyFeatures.F3.isActive());
    }

}
