package org.togglz.core.user.jaas;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Principal;
import java.util.Iterator;

import javax.security.auth.Subject;

import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.FeatureUserProvider;
import org.togglz.core.user.SimpleFeatureUser;


public class JAASUserProvider implements FeatureUserProvider {

    @Override
    public FeatureUser getCurrentUser() {

        AccessControlContext acc = AccessController.getContext();
        if (acc != null) {
            Subject subject = Subject.getSubject(acc);
            if (subject != null) {
                Iterator<Principal> iter = subject.getPrincipals().iterator();
                if (iter.hasNext()) {
                    return new SimpleFeatureUser(iter.next().getName(), false);
                }
            }
        }

        return null;
    }

}
