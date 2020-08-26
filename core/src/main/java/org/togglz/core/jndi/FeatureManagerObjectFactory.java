package org.togglz.core.jndi;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.manager.TogglzConfig;

/**
 * <p>
 * Implementation of {@link ObjectFactory} that creates a {@link FeatureManager}.
 * </p>
 * 
 * <p>
 * The method {@link #getConfiguration(Object)} is used to obtain the {@link TogglzConfig} instance to use for building the
 * manager. The default implementation assumes that the supplied object is a {@link Reference} with an {@link RefAddr} element
 * with the name <code>togglzConfig</code>. This behavior can be changed by overwriting this method.
 * </p>
 * 
 * @author Christian Kaltepoth
 * 
 * @see "http://tomcat.apache.org/tomcat-7.0-doc/jndi-resources-howto.html#Adding_Custom_Resource_Factories"
 */
public class FeatureManagerObjectFactory implements ObjectFactory {

    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) {
        TogglzConfig config = getConfiguration(obj);
        if (config == null) {
            throw new IllegalArgumentException("Unable to find TogglzConfig implementation..");
        }
        return new FeatureManagerBuilder().togglzConfig(config).build();
    }

    /**
     * This class looks up the {@link TogglzConfig} to use when building the {@link FeatureManager}.
     * 
     * @param obj The {@link Object} obtained from {@link #getObjectInstance(Object, Name, Context, Hashtable)}.
     */
    protected TogglzConfig getConfiguration(Object obj) {
        if (obj instanceof Reference) {
            Reference reference = (Reference) obj;

            Enumeration<RefAddr> addrs = reference.getAll();
            while (addrs.hasMoreElements()) {
                RefAddr refAddr = addrs.nextElement();

                if ("togglzConfig".equals(refAddr.getType())) {
                    String classname = refAddr.getContent().toString().trim();
                    return (TogglzConfig) createInstance(classname);
                }
            }
        }
        return null;
    }

    /**
     * Creates an instance of the supplied class.
     */
    protected Object createInstance(String classname) {
        // get the classloader to use
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = this.getClass().getClassLoader();
        }

        // create an instance of the class using the default constructor
        try {
            return classLoader.loadClass(classname).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
}
