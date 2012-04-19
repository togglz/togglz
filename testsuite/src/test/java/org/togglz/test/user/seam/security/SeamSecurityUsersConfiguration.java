package org.togglz.test.user.seam.security;

import java.util.Arrays;

import javax.inject.Inject;

import org.togglz.core.Feature;
import org.togglz.core.config.TogglzConfig;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.user.UserProvider;
import org.togglz.seam.security.SeamSecurityUserProvider;
import org.togglz.test.user.UserDependentFeature;


public class SeamSecurityUsersConfiguration implements TogglzConfig {

    @Inject
    private SeamSecurityUserProvider featureUserProvider;

    @Override
    public Class<? extends Feature> getFeatureClass() {
        return UserDependentFeature.class;
    }

    @Override
    public StateRepository getStateRepository() {
        InMemoryStateRepository repository = new InMemoryStateRepository();
        repository.setFeatureState(new FeatureState(UserDependentFeature.DISABLED, false));
        repository.setFeatureState(new FeatureState(UserDependentFeature.ENABLED_FOR_ALL, true));
        repository.setFeatureState(new FeatureState(UserDependentFeature.ENABLED_FOR_CK, true, Arrays.asList("ck")));
        return repository;
    }

    @Override
    public UserProvider getUserProvider() {
        return featureUserProvider;
    }

}
