package org.togglz.testing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.util.UntypedFeature;

public class TestFeatureManagerTest {

    private final TestFeatureManager manager = new TestFeatureManager(MyFeatures.class);

    @Test
    public void featureShouldBeInactiveByDefault() {
        assertFalse(manager.isActive(MyFeatures.ONE));
    }

    @Test
    public void shouldToggleIndividualFeature() {

        // enable
        manager.enable(MyFeatures.ONE);
        assertTrue(manager.isActive(MyFeatures.ONE));

        // disable
        manager.disable(MyFeatures.ONE);
        assertFalse(manager.isActive(MyFeatures.ONE));

    }

    @Test
    public void shouldToggleAllFeatures() {

        // enable
        manager.enableAll();
        assertTrue(manager.isActive(MyFeatures.ONE));
        assertTrue(manager.isActive(MyFeatures.TWO));

        // disable
        manager.disableAll();
        assertFalse(manager.isActive(MyFeatures.ONE));
        assertFalse(manager.isActive(MyFeatures.TWO));

    }

    // this should be fixed
    @Test
    @Ignore
    public void shouldSupportTogglingUntypedFeature() {

        // enable
        manager.enable(new UntypedFeature("ONE"));
        assertTrue(manager.isActive(MyFeatures.ONE));

        // disable
        manager.disable(new UntypedFeature("ONE"));
        assertFalse(manager.isActive(MyFeatures.ONE));

    }

    // this should be fixed
    @Test
    @Ignore
    public void shouldSupportReadingWithUntypedFeature() {

        // enable
        manager.enable(MyFeatures.ONE);
        assertTrue(manager.isActive(new UntypedFeature("ONE")));

        // disable
        manager.disable(MyFeatures.ONE);
        assertFalse(manager.isActive(new UntypedFeature("ONE")));

    }

    private enum MyFeatures implements Feature {

        ONE,
        TWO;

        @Override
        public boolean isActive() {
            throw new UnsupportedOperationException();
        }
    }

}
