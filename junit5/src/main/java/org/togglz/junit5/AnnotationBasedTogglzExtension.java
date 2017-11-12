package org.togglz.junit5;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.togglz.testing.TestFeatureManager;

/**
 * <p>
 *     JUnit Extension, which creates a {@link TestFeatureManager} with all features enabled/disabled.
 * </p>
 *
 * @see AllEnabled
 * @see AllDisabled
 *
 * @author Roland Weisleder
 */
class AnnotationBasedTogglzExtension extends FeatureManagerExtension {

    @Override
    TestFeatureManager createTestFeatureManager(ExtensionContext context) {
        TestFeatureManager featureManager = null;

        Optional<AllEnabled> allEnabled = findAnnotation(context, AllEnabled.class);
        Optional<AllDisabled> allDisabled = findAnnotation(context, AllDisabled.class);
        if (allEnabled.isPresent() && allDisabled.isPresent()) {
            throw new IllegalStateException("Both @AllEnabled and @AllDisabled are present");
        } else if (allEnabled.isPresent()) {
            featureManager = new TestFeatureManager(allEnabled.get().value());
            featureManager.enableAll();
        } else if (allDisabled.isPresent()) {
            featureManager = new TestFeatureManager(allDisabled.get().value());
            featureManager.disableAll();
        }

        return featureManager;
    }

    private <T extends Annotation> Optional<T> findAnnotation(ExtensionContext context, Class<T> annotationClass) {
        Method testMethod = context.getRequiredTestMethod();
        Optional<T> annotation = AnnotationSupport.findAnnotation(testMethod, annotationClass);
        if (annotation.isPresent()) {
            return annotation;
        }

        Class<?> testClass = context.getRequiredTestClass();
        return AnnotationSupport.findAnnotation(testClass, annotationClass);
    }
}
