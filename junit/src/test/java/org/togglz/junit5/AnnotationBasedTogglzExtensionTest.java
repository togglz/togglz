package org.togglz.junit5;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.togglz.testing.TestFeatureManager;

/**
 * @author Roland Weisleder
 */
class AnnotationBasedTogglzExtensionTest {

    @Test
    @AllEnabled(MyFeatures.class)
    void allEnabled() {
        assertTrue(MyFeatures.ONE.isActive());
        assertTrue(MyFeatures.TWO.isActive());
        assertTrue(MyFeatures.THREE.isActive());
    }

    @Test
    @AllDisabled(MyFeatures.class)
    void allDisabled() {
        assertFalse(MyFeatures.ONE.isActive());
        assertFalse(MyFeatures.TWO.isActive());
        assertFalse(MyFeatures.THREE.isActive());
    }

    @Test
    @AllEnabled(MyFeatures.class)
    void testFeatureManagerParameter(TestFeatureManager featureManager) {
        assertTrue(MyFeatures.ONE.isActive());

        featureManager.disable(MyFeatures.ONE);
        assertFalse(MyFeatures.ONE.isActive());

        featureManager.enable(MyFeatures.ONE);
        assertTrue(MyFeatures.ONE.isActive());
    }

    @Nested
    @AllEnabled(MyFeatures.class)
    class AllEnabledClassTest {

        @Test
        void allEnabled() {
            assertTrue(MyFeatures.ONE.isActive());
            assertTrue(MyFeatures.TWO.isActive());
            assertTrue(MyFeatures.THREE.isActive());
        }

    }

    @Nested
    @AllDisabled(MyFeatures.class)
    class AllDisabledClassTest {

        @Test
        void allDisabled() {
            assertFalse(MyFeatures.ONE.isActive());
            assertFalse(MyFeatures.TWO.isActive());
            assertFalse(MyFeatures.THREE.isActive());
        }

    }

    @AllEnabled(MyFeatures.class)
    abstract static class EnabledParentTest {

        @Test
        void methodInParentClass() {
            assertTrue(MyFeatures.ONE.isActive());
            assertTrue(MyFeatures.TWO.isActive());
            assertTrue(MyFeatures.THREE.isActive());
        }

        @Test
        void testFeatureManagerParameter(TestFeatureManager featureManager) {
            assertNotNull(featureManager);
        }
    }

    @Nested
    class EnabledChildTest extends EnabledParentTest {}

    @AllDisabled(MyFeatures.class)
    abstract static class DisabledParentTest {

        @Test
        void methodInParentClass() {
            assertFalse(MyFeatures.ONE.isActive());
            assertFalse(MyFeatures.TWO.isActive());
            assertFalse(MyFeatures.THREE.isActive());
        }

        @Test
        void testFeatureManagerParameter(TestFeatureManager featureManager) {
            assertNotNull(featureManager);
        }
    }

    @Nested
    class DisabledChildTest extends DisabledParentTest {}

}
