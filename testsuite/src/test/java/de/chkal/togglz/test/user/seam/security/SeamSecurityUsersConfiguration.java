package de.chkal.togglz.test.user.seam.security;

import java.util.Arrays;

import javax.inject.Inject;

import de.chkal.togglz.core.Feature;
import de.chkal.togglz.core.config.FeatureManagerConfiguration;
import de.chkal.togglz.core.manager.FeatureState;
import de.chkal.togglz.core.repository.FeatureStateRepository;
import de.chkal.togglz.core.repository.mem.InMemoryRepository;
import de.chkal.togglz.core.user.FeatureUserProvider;
import de.chkal.togglz.seam.security.SeamSecurityFeatureUserProvider;
import de.chkal.togglz.test.user.UserDependentFeature;

public class SeamSecurityUsersConfiguration implements FeatureManagerConfiguration {

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
