package org.togglz.spring.proxy;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashSet;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.InitializingBean;
import org.togglz.core.Feature;
import org.togglz.core.proxy.FeatureProxyInvocationHandler;
import org.togglz.core.util.NamedFeature;
import org.togglz.core.util.Validate;

/**
 * <p>
 * Implementation of {@link FactoryBean} that creates a proxy that delegates invocation to one of two target beans depending on
 * that state of a given feature.
 * </p>
 * 
 * <p>
 * You can use the factory like this:
 * </p>
 * 
 * <pre>
 * &lt;bean id="someService" class="org.togglz.spring.proxy.FeatureProxyFactoryBean"&gt;
 *   &lt;property name="feature" value="FEATURE_ONE" /&gt;
 *   &lt;property name="active" ref="newServiceImpl" /&gt;
 *   &lt;property name="inactive" ref="oldServiceImpl" /&gt;
 * &lt;/bean&gt;
 * </pre>
 * 
 * @author Christian Kaltepoth
 */
public class FeatureProxyFactoryBean implements FactoryBean<Object>, InitializingBean {

    private String feature;

    private Object active;

    private Object inactive;

    private Class<?> proxyType;
    
    private boolean initialized = false;

    @Override
    public void afterPropertiesSet() throws Exception {
        Validate.notBlank(feature, "The 'feature' property is required");
        Validate.notNull(active, "The 'active' property is required");
        Validate.notNull(inactive, "The 'inactive' property is required");
        if (proxyType != null && !proxyType.isInterface()) {
            throw new IllegalArgumentException(proxyType.getClass().getName() + " is not an interface");
        }
        initialized = true;
    }

    @Override
    public Object getObject() throws Exception {

        // make sure the factory is fully initialized
        if (!initialized) {
            throw new FactoryBeanNotInitializedException();
        }

        // create the invocation handler that switches between implementations
        Feature namedFeature = new NamedFeature(feature);
        FeatureProxyInvocationHandler proxy = new FeatureProxyInvocationHandler(namedFeature, active, inactive);

        // obtain the interface for which to create the proxy
        Class<?> proxyType = getEffectiveProxyType();

        // create the proxy
        return Proxy.newProxyInstance(getSuitableClassLoader(), new Class<?>[] { proxyType }, proxy);

    }

    private Class<?> getEffectiveProxyType() {

        // prefer the business interface manually set by the user
        if (proxyType != null) {
            return proxyType;
        }

        // check which interfaces the both delegates implements
        HashSet<Class<?>> activeInterfaces = new HashSet<Class<?>>(Arrays.asList(active.getClass().getInterfaces()));
        HashSet<Class<?>> inactiveInterfaces = new HashSet<Class<?>>(Arrays.asList(inactive.getClass().getInterfaces()));

        // build the intersection
        activeInterfaces.retainAll(inactiveInterfaces);

        // we need exactly one interface to share
        if (activeInterfaces.size() != 1) {
            throw new IllegalArgumentException("The active and the inactive class must share exactly one interface");
        }

        return activeInterfaces.iterator().next();

    }

    private ClassLoader getSuitableClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = this.getClass().getClassLoader();
        }
        return classLoader;
    }

    @Override
    public Class<?> getObjectType() {
        if (initialized) {
            return getEffectiveProxyType();
        } else {
            return null;
        }
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public Object getActive() {
        return active;
    }

    public void setActive(Object active) {
        this.active = active;
    }

    public Object getInactive() {
        return inactive;
    }

    public void setInactive(Object inactive) {
        this.inactive = inactive;
    }

    public Class<?> getProxyType() {
        return proxyType;
    }

    public void setProxyType(Class<?> proxyType) {
        this.proxyType = proxyType;
    }

}
