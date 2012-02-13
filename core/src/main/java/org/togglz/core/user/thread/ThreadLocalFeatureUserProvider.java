package org.togglz.core.user.thread;

import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.FeatureUserProvider;

public class ThreadLocalFeatureUserProvider implements FeatureUserProvider {

    private static ThreadLocal<FeatureUser> threadLocal = new ThreadLocal<FeatureUser>();

    public static void setFeatureUser(FeatureUser featureUser) {
        if (featureUser != null && threadLocal.get() != null) {
            throw new IllegalStateException("setFeatureUser() called for a "
                    + "thread that already has one associated with it. It's likely that the FeatureUser "
                    + "is not correctly removed from the thread before it is put back into the thread pool.");
        }
        threadLocal.set(featureUser);
    }

    @Override
    public FeatureUser getCurrentUser() {
        return threadLocal.get();
    }

}
