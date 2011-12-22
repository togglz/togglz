package de.chkal.togglz.core.holder;

import de.chkal.togglz.core.user.FeatureUser;

public class FeatureUserHolder {

    private static ThreadLocal<FeatureUser> threadLocal = new ThreadLocal<FeatureUser>();

    public static void setFeatureUser(FeatureUser featureUser) {
        if (featureUser != null && threadLocal.get() != null) {
            throw new IllegalStateException("FeatureUserHolder.setFeatureUser() called for a "
                    + "thread that already has one associated with it. It's likely that the FeatureUser "
                    + "is not correctly removed from the thread before it is put back into the thread pool.");
        }
        threadLocal.set(featureUser);
    }

    public static FeatureUser getFeatureUser() {
        FeatureUser featureUser = threadLocal.get();
        if (featureUser == null) {
            throw new IllegalStateException("FeatureUser unavailable for the current thread.");
        }
        return featureUser;
    }

}
