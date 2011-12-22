package de.chkal.togglz.core.holder;

import de.chkal.togglz.core.manager.FeatureManager;

public class FeatureManagerHolder {

    private static ThreadLocal<FeatureManager> threadLocal = new ThreadLocal<FeatureManager>();

    public static void setFeatureManager(FeatureManager featureManager) {
        if (featureManager != null && threadLocal.get() != null) {
            throw new IllegalStateException("FeatureManagerHolder.setFeatureManager() called for a "
                    + "thread that already has one associated with it. It's likely that the FeatureManager "
                    + "is not correctly removed from the thread before it is put back into the thread pool.");
        }
        threadLocal.set(featureManager);
    }

    public static FeatureManager getFeatureManager() {
        FeatureManager featureManager = threadLocal.get();
        if (featureManager == null) {
            throw new IllegalStateException("FeatureManager unavailable for the current thread.");
        }
        return featureManager;
    }

}
