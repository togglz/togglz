package de.chkal.togglz.core.user.provider;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Principal;
import java.util.Iterator;

import javax.security.auth.Subject;

import de.chkal.togglz.core.user.FeatureUser;
import de.chkal.togglz.core.user.PermissionEvaluator;
import de.chkal.togglz.core.user.SimpleFeatureUser;

public class JAASUserProvider implements FeatureUserProvider {

    private final PermissionEvaluator permissionEvaluator;

    public JAASUserProvider(PermissionEvaluator permissionEvaluator) {
        this.permissionEvaluator = permissionEvaluator;
    }

    @Override
    public FeatureUser getCurrentUser() {

        AccessControlContext acc = AccessController.getContext();
        if (acc != null) {
            Subject subject = Subject.getSubject(acc);
            if (subject != null) {
                Iterator<Principal> iter = subject.getPrincipals().iterator();
                if (iter.hasNext()) {
                    return new SimpleFeatureUser(iter.next().getName(), permissionEvaluator);
                }
            }
        }

        return null;
    }

}
