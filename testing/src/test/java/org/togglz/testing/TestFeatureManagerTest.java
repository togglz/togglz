package org.togglz.testing;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.togglz.core.Feature;
import org.togglz.core.util.NamedFeature;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    public void shouldSupportTogglingUntypedFeature() {

        // enable
        manager.enable(new NamedFeature("ONE"));
        assertTrue(manager.isActive(MyFeatures.ONE));

        // disable
        manager.disable(new NamedFeature("ONE"));
        assertFalse(manager.isActive(MyFeatures.ONE));

    }

    @Test
    public void shouldSupportReadingWithNamedFeature() {

        // enable
        manager.enable(MyFeatures.ONE);
        assertTrue(manager.isActive(new NamedFeature("ONE")));

        // disable
        manager.disable(MyFeatures.ONE);
        assertFalse(manager.isActive(new NamedFeature("ONE")));

    }

    private enum MyFeatures implements Feature {
        ONE,
        TWO;
    }

}
