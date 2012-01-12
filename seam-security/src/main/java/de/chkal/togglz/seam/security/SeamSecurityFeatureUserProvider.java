package de.chkal.togglz.seam.security;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.seam.security.Identity;

import de.chkal.togglz.core.user.FeatureUser;
import de.chkal.togglz.core.user.FeatureUserProvider;
import de.chkal.togglz.core.user.SimpleFeatureUser;

@ApplicationScoped
public class SeamSecurityFeatureUserProvider implements FeatureUserProvider {

    @Inject
    private Identity identity;

    private final String role;

    private final String group;

    private final String groupType;

    public SeamSecurityFeatureUserProvider(String role, String group, String groupType) {
        this.role = role;
        this.group = group;
        this.groupType = groupType;
    }

    @Override
    public FeatureUser getCurrentUser() {

        if (identity != null) {

            String name = identity.getUser().getId();
            boolean featureAdmin = identity.hasRole(role, group, groupType);

            return new SimpleFeatureUser(name, featureAdmin);

        }

        return null;

    }
}
