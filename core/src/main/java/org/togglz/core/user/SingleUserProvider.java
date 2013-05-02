package org.togglz.core.user;

/**
 * A very simple implementation of {@link UserProvider} which always returns the supplied user.
 */
public class SingleUserProvider implements UserProvider {

    private final FeatureUser user;

    public SingleUserProvider(String name) {
        this(name, false);
    }

    public SingleUserProvider(String name, boolean featureAdmin) {
        this(new SimpleFeatureUser(name, featureAdmin));
    }

    public SingleUserProvider(FeatureUser featureUser) {
        this.user = featureUser;
    }

    @Override
    public FeatureUser getCurrentUser() {
        return user;
    }

}
