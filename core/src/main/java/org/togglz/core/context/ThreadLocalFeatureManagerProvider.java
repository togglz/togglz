package org.togglz.core.context;

import org.togglz.core.manager.FeatureManager;
import org.togglz.core.spi.FeatureManagerProvider;

/**
 * 
 * This implementation of {@link FeatureManagerProvider} allows to store the {@link FeatureManager} in a {@link ThreadLocal} for
 * the current thread.
 * 
 * Please note that it is very important to cleanup the {@link ThreadLocal} by calling {@link #release()} before the thread is
 * put back into a thread pool.
 * 
 * @author Christian Kaltepoth
 * 
 */
public class ThreadLocalFeatureManagerProvider implements FeatureManagerProvider {

    private static final ThreadLocal<FeatureManager> threadLocal = new ThreadLocal<>();

    /**
     * Store the supplied {@link FeatureManager} in the thread context. After calling this method all calls of
     * {@link #getFeatureManager()} from the active thread will return this feature manager. Please don't forget to call
     * {@link #release()} before the thread is put back to a thread pool to prevent memory leaks.
     * 
     * @param featureManager The {@link FeatureManager} to store
     */
    public static void bind(FeatureManager featureManager) {
        if (featureManager != null && threadLocal.get() != null) {
            throw new IllegalStateException("bind() called for a thread that already has a FeatureManager " +
                    "associated with it. It's likely that the FeatureManager is not correctly removed " +
                    "from the thread before it is put back into the thread pool.");
        }
        threadLocal.set(featureManager);
    }

    /**
     * Removes the {@link FeatureManager} associated with the current thread from the thread's context. It's required to always
     * call this method before a thread is put back to a thread pool.
     */
    public static void release() {
        threadLocal.set(null);
    }

    @Override
    public int priority() {
        return 50;
    }

    @Override
    public FeatureManager getFeatureManager() {
        return threadLocal.get();
    }

}
