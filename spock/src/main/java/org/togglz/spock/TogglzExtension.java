package org.togglz.spock;

import org.spockframework.runtime.extension.IAnnotationDrivenExtension;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.runtime.model.parallel.ExclusiveResource;
import org.spockframework.runtime.model.parallel.ResourceAccessMode;
import org.togglz.testing.TestFeatureManagerProvider;

public class TogglzExtension implements IAnnotationDrivenExtension<Togglz> {
    public static final String TOGGLZ_FEATURE_RESOURCE = TestFeatureManagerProvider.class.getCanonicalName();

    /**
     * As the TestFeatureManagerProvider is a shared resource, we need to acquire a read write lock.
     */
    private static final ExclusiveResource TOGGLZ_EXCLUSIVE_RESOURCE = new ExclusiveResource(TOGGLZ_FEATURE_RESOURCE,
            ResourceAccessMode.READ_WRITE);

    private static final TogglzInjectionInterceptor INJECTION_INTERCEPTOR = new TogglzInjectionInterceptor();

    @Override
    public void visitSpecAnnotation(Togglz specAnnotation, SpecInfo spec) {
        if (!spec.getIsBottomSpec()) {
            // The annotation is inherited so backoff if we are not the bottom spec
            return;
        }

        TogglzInterceptor specInterceptor = new TogglzInterceptor(specAnnotation, null);

        spec.addExclusiveResource(TOGGLZ_EXCLUSIVE_RESOURCE);
        spec.getAllFeatures().forEach(feature -> {
            Togglz featureAnnotation = feature.getFeatureMethod().getAnnotation(Togglz.class);
            TogglzInterceptor featureInterceptor = featureAnnotation == null ? specInterceptor :
                                                   new TogglzInterceptor(specAnnotation, featureAnnotation);
            feature.addIterationInterceptor(featureInterceptor);
            feature.getFeatureMethod().addInterceptor(INJECTION_INTERCEPTOR);
        });
    }

    @Override
    public void visitFeatureAnnotation(Togglz featureAnnotation, FeatureInfo feature) {
        if (feature.getSpec().isAnnotationPresent(Togglz.class)) {
            // The interceptor was already installed by the spec annotation
            return;
        }
        feature.addExclusiveResource(TOGGLZ_EXCLUSIVE_RESOURCE);
        feature.addIterationInterceptor(new TogglzInterceptor(null, featureAnnotation));
        feature.getFeatureMethod().addInterceptor(INJECTION_INTERCEPTOR);
    }
}
