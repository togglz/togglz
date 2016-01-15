package org.togglz.spring.util;

import org.springframework.context.ApplicationContext;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores the {@link ApplicationContext} for the current context class loader.
 *
 * @author Christian Kaltepoth
 */
public class ContextClassLoaderApplicationContextHolder {

    private static final ConcurrentHashMap<ClassLoader, ApplicationContext> contextMap = new ConcurrentHashMap<ClassLoader, ApplicationContext>();

    /**
     * Returns the {@link ApplicationContext} bound to the current context class loader or any of
     * its parent class loaders. Walking up the class loader hierarchy is required to support
     * special class loader scenarios like Spring Boot's reloading mechanism.
     */
    public static ApplicationContext get() {
        ClassLoader classLoader = getContextClassLoader();
        while (classLoader != null) {
            ApplicationContext applicationContext = contextMap.get(classLoader);
            if (applicationContext != null) {
                return applicationContext;
            }
            classLoader = classLoader.getParent();
        }
        return null;
    }

    /**
     * Binds the {@link ApplicationContext} to the current context class loader .
     */
    public static void bind(ApplicationContext context) {
        Object old = contextMap.putIfAbsent(getContextClassLoader(), context);
        if (old != null) {
            throw new IllegalStateException(
                "There is already a ApplicationContext associated with the context ClassLoader of the current thread!");
        }
    }

    /**
     * Releases the {@link ApplicationContext} associated with the current context class loader.
     */
    public static void release() {
        contextMap.remove(getContextClassLoader());
    }

    private static ClassLoader getContextClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            throw new IllegalStateException("Unable to get the context class loader for the current thread!");
        }
        return classLoader;
    }

}
