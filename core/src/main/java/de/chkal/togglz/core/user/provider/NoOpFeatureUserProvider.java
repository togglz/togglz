package de.chkal.togglz.core.user.provider;

import de.chkal.togglz.core.user.FeatureUser;

public class NoOpFeatureUserProvider implements FeatureUserProvider {

    @Override
    public FeatureUser getCurrentUser() {
        return null;
    }

}
