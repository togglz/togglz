package de.chkal.togglz.core.user;


public class NoOpFeatureUserProvider implements FeatureUserProvider {

    @Override
    public FeatureUser getCurrentUser() {
        return null;
    }

}
