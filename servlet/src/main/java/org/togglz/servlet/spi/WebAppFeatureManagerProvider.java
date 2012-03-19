package org.togglz.servlet.spi;

import java.util.concurrent.ConcurrentHashMap;

import org.togglz.core.manager.FeatureManager;
import org.togglz.core.spi.FeatureManagerProvider;

/**
 * 
 * This implementation of {@link FeatureManagerProvider} stores one {@link FeatureManager} for each context classloader.
 * 
 * @author Christian Kaltepoth
 * 
 */
public class WebAppFeatureManagerProvider implements FeatureManagerProvider {

    private static final ConcurrentHashMap<ClassLoader, FeatureManager> managerMap = new ConcurrentHashMap<ClassLoader, FeatureManager>();

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public FeatureManager getFeatureManager() {
        return managerMap.get(getContextClassLoader());
    }

    /**
     * Binds the {@link FeatureManager} to the current context classloader.
     * 
     * @param featureManager The manager to store
     */
    public static void bindFeatureManager(FeatureManager featureManager) {
        Object old = managerMap.putIfAbsent(getContextClassLoader(), featureManager);
        if (old != null) {
            throw new IllegalStateException(
                    "There is already a FeatureManager associated with the context ClassLoader of the current thread!");
        }
    }

    /**
     * Removes the {@link FeatureManager} associated with the current context classloader from the internal datastructure.
     */
    public static void unbindFeatureManager() {
        managerMap.remove(getContextClassLoader());
    }

    private static ClassLoader getContextClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            throw new IllegalStateException("Unable to get the context ClassLoader for the current thread!");
        }
        return classLoader;
    }

}
