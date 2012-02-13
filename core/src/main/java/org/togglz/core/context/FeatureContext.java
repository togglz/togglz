package org.togglz.core.context;

import java.util.concurrent.ConcurrentHashMap;

import org.togglz.core.manager.FeatureManager;


/**
 * 
 * The {@link FeatureContext} stores one {@link FeatureManager} for each context classloader. This class is typically used to
 * obtai the {@link FeatureManager} from application code.
 * 
 * @author Christian Kaltepoth
 * 
 */
public class FeatureContext {

    private static final ConcurrentHashMap<ClassLoader, FeatureManager> managerMap = new ConcurrentHashMap<ClassLoader, FeatureManager>();

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

    /**
     * Returns the {@link FeatureManager} associated with the current context classloader. The method will throw a runtime
     * exception if not {@link FeatureManager} manager has been bound to the context classloader before using
     * {@link #bindFeatureManager(FeatureManager)}.
     * 
     * @return The manager associalted with the current context classloader, never <code>null</code>
     */
    public static FeatureManager getFeatureManager() {
        FeatureManager featureManager = managerMap.get(getContextClassLoader());
        if (featureManager == null) {
            throw new IllegalStateException("FeatureManager is not bound to the context ClassLoader of the current thread!");
        }
        return featureManager;
    }

    private static ClassLoader getContextClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            throw new IllegalStateException("Unable to get the context ClassLoader for the current thread!");
        }
        return classLoader;
    }

}
