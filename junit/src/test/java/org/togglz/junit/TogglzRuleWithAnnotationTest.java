package org.togglz.junit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class TogglzRuleWithAnnotationTest {

    @Rule
    public TogglzRule togglzRule = TogglzRule.allDisabled(MyFeatures.class);

    @Before
    public void setUp()
    {
        Assert.assertTrue(MyFeatures.FEATURE_ONE.isActive());
    }
    
    @Test
    @WithFeature(type=MyFeatures.class, value="FEATURE_ONE")
    public void test_enabledByAnnotation()
    {
        Assert.assertTrue(MyFeatures.FEATURE_ONE.isActive());
    }

}
