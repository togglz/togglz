package org.togglz.core.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.LazyResolvingFeatureManager;

/**
 * A {@link InvocationHandler} implementation that delegates invocation to one of two object depending on the state of the
 * specified feature.
 * 
 * @author Christian Kaltepoth
 */
public class FeatureProxyInvocationHandler implements InvocationHandler {

    private final Feature feature;

    private final Object active;

    private final Object inactive;

    private final FeatureManager featureManager;

    public FeatureProxyInvocationHandler(Feature feature, Object active, Object inactive) {
        this(feature, active, inactive, new LazyResolvingFeatureManager());
    }

    public FeatureProxyInvocationHandler(Feature feature, Object active, Object inactive, FeatureManager featureManager) {
        this.feature = feature;
        this.active = active;
        this.inactive = inactive;
        this.featureManager = featureManager;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object target = featureManager.isActive(feature) ? active : inactive;
        try {
        	return method.invoke(target, args);
        } catch (InvocationTargetException ex) {
        	throw ex.getCause();
        }
        
    }

    public Feature getFeature() {
        return feature;
    }

    public Object getActive() {
        return active;
    }

    public Object getInactive() {
        return inactive;
    }

}
