package org.togglz.spring.web;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.manager.FeatureManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This interceptor checks if a controller or controller method is annotated with the
 * {@link FeaturesAreActive} annotation to determine if a controller should be
 * activated or not.
 * <p>
 * Set the togglz.web.register-feature-interceptor to {@code true} to activate this interceptor.
 *
 * @author ractive
 * @author m-schroeer
 */
public class FeatureInterceptor implements HandlerInterceptor {

    /**
     * Used to store annotations in a ConcurrentHashMap which does not allow storing {@code null} values.
     *
     * @param <A> annotation type
     */
    private static final class AnnotationHolder<A extends Annotation> {

        private final A annotation;

        public AnnotationHolder(final A annotation) {
            this.annotation = annotation;
        }

        public A getAnnotation() {
            return this.annotation;
        }

        public boolean hasAnnotation() {
            return this.annotation != null;
        }
    }

    /**
     * Caches the annotations on the {@link HandlerMethod}s to avoid expensive reflection calls for every request.
     */
    private final ConcurrentHashMap<HandlerMethod, AnnotationHolder<FeaturesAreActive>> annotations = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            final HandlerMethod handlerMethod = (HandlerMethod) handler;
            AnnotationHolder<FeaturesAreActive> annotationHolder = this.annotations.get(handlerMethod);
            if (annotationHolder == null) {
                final FeaturesAreActive foundAnnotation = handlerAnnotation(handlerMethod, FeaturesAreActive.class);
                this.annotations.putIfAbsent(handlerMethod, new AnnotationHolder<>(foundAnnotation));
                annotationHolder = this.annotations.get(handlerMethod);
            }

            if (annotationHolder.hasAnnotation()) {
                final FeaturesAreActive featuresAreActiveAnnotation = annotationHolder.getAnnotation();

                final Set<String> annotationFeatureNames = Stream.of(
                        featuresAreActiveAnnotation.features())
                        .collect(Collectors.toSet());

                final FeatureManager featureManager = FeatureContext.getFeatureManager();

                if (!getFeatureNames(featureManager).containsAll(annotationFeatureNames)) {
                    throw new IllegalArgumentException("At least one given feature of '" + annotationFeatureNames + "' is not a feature!");
                }

                final boolean allFeaturesOfAnnotationMatch = featureManager.getFeatures()
                        .stream()
                        // reduce to features of annotation
                        .filter(feature -> annotationFeatureNames.contains(feature.name()))
                        .allMatch(featureManager::isActive);

                if (!allFeaturesOfAnnotationMatch) {
                    final int errorStatusCode = getErrorStatus(featuresAreActiveAnnotation).value();
                    response.sendError(errorStatusCode);
                    return false;
                }
            }
        }
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    // TODO: When deprecated field FeaturesAreActive#responseStatus is removed, this method could be removed as well and
    //       be replaced by inline call to FeaturesAreActive#errorResponseStatus.
    private HttpStatus getErrorStatus(final FeaturesAreActive annotation) {
        final HttpStatus responseHttpStatus = HttpStatus.valueOf(annotation.responseStatus());

        final HttpStatus errorResponseStatus = annotation.errorResponseStatus();

        if (errorResponseStatus == responseHttpStatus) {
            return errorResponseStatus;
        } else {
            // errorResponseStatus != responseHttpStatus
            if (errorResponseStatus == FeaturesAreActive.DEFAULT_ERROR_RESPONSE_STATUS) {
                // return the non-default value
                return responseHttpStatus;
            } else if (responseHttpStatus == FeaturesAreActive.DEFAULT_ERROR_RESPONSE_STATUS) {
                // return the non-default value
                return errorResponseStatus;
            } else {
                throw new IllegalArgumentException("'responseStatus' and 'errorResponseStatus' cannot be both non-default and different!");
            }
        }
    }

    protected static <A extends Annotation> A handlerAnnotation(final HandlerMethod handlerMethod, final Class<A> annotationClass) {
        A annotation = handlerMethod.getMethodAnnotation(annotationClass);
        if (annotation == null) {
            annotation = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), annotationClass);
        }
        return annotation;
    }

    protected static Set<String> getFeatureNames(final FeatureManager featureManager) {
        return featureManager.getFeatures().stream()
                .map(Feature::name)
                .collect(Collectors.toSet());
    }
}
