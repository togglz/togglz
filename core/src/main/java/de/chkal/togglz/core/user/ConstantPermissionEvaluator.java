package de.chkal.togglz.core.user;

public class ConstantPermissionEvaluator implements PermissionEvaluator {

    private final boolean featureAdmin;

    public ConstantPermissionEvaluator(boolean featureAdmin) {
        this.featureAdmin = featureAdmin;
    }

    @Override
    public boolean isFeatureAdmin(FeatureUser user) {
        return featureAdmin;
    }

}
