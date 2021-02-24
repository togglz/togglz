package org.togglz.junit.vary;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(FeatureVariations.class)
public class FeatureVariationsTest {

    @Variations
    public static VariationSet<MyFeatures> getPermutations() {
        return VariationSetBuilder.create(MyFeatures.class)
                .enable(MyFeatures.F1)
                .vary(MyFeatures.F2)
                .vary(MyFeatures.F3);
    }

    @Test
    public void test() {
        assertTrue(MyFeatures.F1.isActive());
        assertTrue(MyFeatures.F2.isActive() || !MyFeatures.F2.isActive());
        assertTrue(MyFeatures.F3.isActive() || !MyFeatures.F3.isActive());
    }

}
