package de.chkal.togglz.core.user;

public class SimpleFeatureUser implements FeatureUser {

    private final String name;
    private final PermissionEvaluator permissionEvaluator;

    public SimpleFeatureUser(String name, PermissionEvaluator permissionEvaluator) {
        this.name = name;
        this.permissionEvaluator = permissionEvaluator;
    }

    public SimpleFeatureUser(String name, boolean featureAdmin) {
        this.name = name;
        this.permissionEvaluator = new ConstantPermissionEvaluator(featureAdmin);
    }

    public String getName() {
        return name;
    }

    public boolean isFeatureAdmin() {
        return permissionEvaluator.isFeatureAdmin(this);
    }

}
