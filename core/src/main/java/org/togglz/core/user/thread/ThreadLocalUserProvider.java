package org.togglz.core.user.thread;

import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.UserProvider;

/**
 * 
 * This implementation of {@link UserProvider} is very useful if authentication has been implemented using a servlet
 * filter. It allows to store the current user in a {@link ThreadLocal} for the active thread. See the following code for an
 * example for how to user this class.
 * 
 * <pre>
 * FeatureUser user = ....
 * 
 * ThreadLocalFeatureUserProvider.bind(user);
 * try {
 *     chain.doFilter(request, response);
 * } finally {
 *     ThreadLocalFeatureUserProvider.release();
 * }
 * </pre>
 * 
 * Please not that it is very important to remove the user from the provider after
 * 
 * @author Christian Kaltepoth
 * 
 */
public class ThreadLocalUserProvider implements UserProvider {

    private static final ThreadLocal<FeatureUser> threadLocal = new ThreadLocal<>();

    /**
     * Store the supplied FeatureUser in the thread context. After calling this method all calls of {@link #getCurrentUser()}
     * from the active thread will return this feature user. Please don't forget to call {@link #release()} before the thread is
     * put back to a thread pool to prevent memory leaks.
     * 
     * @param featureUser The feature user to store
     */
    public static void bind(FeatureUser featureUser) {
        if (featureUser != null && threadLocal.get() != null) {
            throw new IllegalStateException("setFeatureUser() called for a "
                    + "thread that already has one associated with it. It's likely that the FeatureUser "
                    + "is not correctly removed from the thread before it is put back into the thread pool.");
        }
        threadLocal.set(featureUser);
    }

    /**
     * Removes the user associated with the current thread from the thread's context. It's required to always call this method
     * before a thread is put back to a thread pool.
     */
    public static void release() {
        threadLocal.set(null);
    }

    @Override
    public FeatureUser getCurrentUser() {
        return threadLocal.get();
    }

}
