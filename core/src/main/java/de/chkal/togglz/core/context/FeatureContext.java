package de.chkal.togglz.core.context;

import java.util.concurrent.ConcurrentHashMap;

import de.chkal.togglz.core.manager.FeatureManager;

public class FeatureContext {

    private static final ConcurrentHashMap<ClassLoader, FeatureManager> managerMap = new ConcurrentHashMap<ClassLoader, FeatureManager>();

    public static void bindFeatureManager(FeatureManager featureManager) {
        Object old = managerMap.putIfAbsent(getContextClassLoader(), featureManager);
        if (old != null) {
            throw new IllegalStateException(
                    "There is already a FeatureManager associated with the context ClassLoader of the current thread!");
        }
    }

    public static void unbindFeatureManager() {
        managerMap.remove(getContextClassLoader());
    }

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
