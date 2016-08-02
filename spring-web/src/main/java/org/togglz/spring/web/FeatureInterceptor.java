package org.togglz.spring.web;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;

import java.lang.annotation.Annotation;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This interceptor checks if a controller or controller method is annotated with the
 * {@link FeaturesAreActive} annotation to determine if a controller should be 
 * activated or not.
 * <p>
 * Set the togglz.web.register-feature-interceptor to {@code true} to activate this interceptor.
 */
public class FeatureInterceptor extends HandlerInterceptorAdapter {
    /**
     * Used to store annotations in a ConcurrentHashMap which does not allow storing {@code null} values.
     * 
     * @param <A> annotation type
     */
    private static final class AnnotationHolder<A extends Annotation> {
        private final A annotation;
        
        public AnnotationHolder(A annotation) {
            this.annotation = annotation;
        }
        
        public A getAnnotation() {
            return annotation;
        }
        
        public boolean hasAnnotation() {
            return annotation != null;
        }
    }

    /**
     * Caches the annotations on the {@link HandlerMethod}s to avoid expensive reflection calls for every request.
     */
    private ConcurrentHashMap<HandlerMethod, AnnotationHolder<FeaturesAreActive>> annotations = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            AnnotationHolder<FeaturesAreActive> annotationHolder = annotations.get(handlerMethod);
            if (annotationHolder == null) {
                FeaturesAreActive foundAnnotation = handlerAnnotation(handlerMethod, FeaturesAreActive.class);
                annotations.putIfAbsent(handlerMethod, new AnnotationHolder<FeaturesAreActive>(foundAnnotation));
                annotationHolder = annotations.get(handlerMethod);
            }
            
            if (annotationHolder.hasAnnotation()) {
                FeaturesAreActive featuresAreActiveAnnotation = annotationHolder.getAnnotation();
                if (!Enum.class.isAssignableFrom(featuresAreActiveAnnotation.featureClass())) {
                    throw new IllegalArgumentException("The featureClass of the " + FeaturesAreActive.class.getSimpleName() + " annotation must be an enum");
                }
                for (String f : featuresAreActiveAnnotation.features()) {
                    @SuppressWarnings("unchecked")
                    Feature feature = (Feature) enumFrom(f, (Class<? extends Enum<?>>) featuresAreActiveAnnotation.featureClass());
                    if (feature != null && !FeatureContext.getFeatureManager().isActive(feature)) {
                        response.sendError(featuresAreActiveAnnotation.responseStatus());
                        return false;
                    }
                }
            }
        }
        return super.preHandle(request, response, handler);
    }

    protected static <A extends Annotation> A handlerAnnotation(HandlerMethod handlerMethod, Class<A> annotationClass) {
        A annotation = handlerMethod.getMethodAnnotation(annotationClass);
        if (annotation == null) {
            annotation = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), annotationClass);
        }
        return annotation;
    }
    
    protected static <T extends Enum<?>> T enumFrom(String name, Class<T> enumType) {
        if (name != null) {
            for (T item : enumType.getEnumConstants()) {
                if (name.equals(item.name())) {
                    return item;
                }
            }
        }
        return null;
    }
}
