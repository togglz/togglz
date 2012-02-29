package org.togglz.test.user.seam.security;

import java.util.Arrays;

import javax.inject.Inject;

import org.togglz.core.Feature;
import org.togglz.core.config.TogglzConfig;
import org.togglz.core.manager.FeatureState;
import org.togglz.core.repository.FeatureStateRepository;
import org.togglz.core.repository.mem.InMemoryRepository;
import org.togglz.core.user.FeatureUserProvider;
import org.togglz.seam.security.SeamSecurityFeatureUserProvider;
import org.togglz.test.user.UserDependentFeature;


public class SeamSecurityUsersConfiguration implements TogglzConfig {

    @Inject
    private SeamSecurityFeatureUserProvider featureUserProvider;

    @Override
    public Class<? extends Feature> getFeatureClass() {
        return UserDependentFeature.class;
    }

    @Override
    public FeatureStateRepository getFeatureStateRepository() {
        InMemoryRepository repository = new InMemoryRepository();
        repository.setFeatureState(new FeatureState(UserDependentFeature.DISABLED, false));
        repository.setFeatureState(new FeatureState(UserDependentFeature.ENABLED_FOR_ALL, true));
        repository.setFeatureState(new FeatureState(UserDependentFeature.ENABLED_FOR_CK, true, Arrays.asList("ck")));
        return repository;
    }

    @Override
    public FeatureUserProvider getFeatureUserProvider() {
        return featureUserProvider;
    }

}
