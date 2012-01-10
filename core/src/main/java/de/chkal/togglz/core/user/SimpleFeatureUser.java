package de.chkal.togglz.core.user;

public class SimpleFeatureUser implements FeatureUser {

    private final String name;
    private final boolean featureAdmin;

    public SimpleFeatureUser(String name, boolean featureAdmin) {
        this.name = name;
        this.featureAdmin = featureAdmin;
    }

    public String getName() {
        return name;
    }

    public boolean isFeatureAdmin() {
        return featureAdmin;
    }

}
