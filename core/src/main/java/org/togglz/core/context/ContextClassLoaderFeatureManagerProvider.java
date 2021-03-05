package org.togglz.core.context;

import java.util.concurrent.ConcurrentHashMap;

import org.togglz.core.manager.FeatureManager;
import org.togglz.core.spi.FeatureManagerProvider;

/**
 * 
 * This implementation of {@link FeatureManagerProvider} stores one {@link FeatureManager} for each context class loader.
 * 
 * @author Christian Kaltepoth
 * 
 */
public class ContextClassLoaderFeatureManagerProvider implements FeatureManagerProvider {

    private static final ConcurrentHashMap<ClassLoader, FeatureManager> managerMap = new ConcurrentHashMap<>();

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public FeatureManager getFeatureManager() {
        return managerMap.get(contextClassLoader());
    }

    /**
     * Binds the {@link FeatureManager} to the current context class loader .
     */
    public static void bind(FeatureManager featureManager) {
        Object old = managerMap.putIfAbsent(contextClassLoader(), featureManager);
        if (old != null) {
            throw new IllegalStateException(
                "There is already a FeatureManager associated with the context ClassLoader of the current thread!");
        }
    }

    /**
     * Releases the {@link FeatureManager} associated with the current context class loader.
     */
    public static void release() {
        managerMap.remove(contextClassLoader());
    }

    private static ClassLoader contextClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            throw new IllegalStateException("Unable to get the context class loader for the current thread!");
        }
        return classLoader;
    }

}
