package org.togglz.core.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.togglz.core.Feature;

/**
 * A {@link InvocationHandler} implementation that delegates invocation to one of two object depending on the state of the
 * specified feature.
 * 
 * @author Christian Kaltepoth
 */
public class FeatureProxyInvocationHandler implements InvocationHandler {

    private Feature feature;

    private final Object active;

    private final Object inactive;

    public FeatureProxyInvocationHandler(Feature feature, Object active, Object inactive) {
        this.feature = feature;
        this.active = active;
        this.inactive = inactive;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object target = feature.isActive() ? active : inactive;
        return method.invoke(target, args);
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
