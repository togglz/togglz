package de.chkal.togglz.core.user;

public interface PermissionEvaluator {

    boolean isFeatureAdmin(FeatureUser user);
    
}
