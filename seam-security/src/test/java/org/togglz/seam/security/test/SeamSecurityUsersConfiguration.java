package org.togglz.seam.security.test;

import javax.inject.Inject;

import org.togglz.core.Feature;
import org.togglz.core.activation.UsernameActivationStrategy;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.user.UserProvider;
import org.togglz.seam.security.SeamSecurityUserProvider;

public class SeamSecurityUsersConfiguration implements TogglzConfig {

    @Inject
    private SeamSecurityUserProvider featureUserProvider;

    @Override
    public Class<? extends Feature> getFeatureClass() {
        return TestFeature.class;
    }

    @Override
    public StateRepository getStateRepository() {
        InMemoryStateRepository repository = new InMemoryStateRepository();
        repository.setFeatureState(new FeatureState(TestFeature.DISABLED, false));
        repository.setFeatureState(new FeatureState(TestFeature.ENABLED_FOR_ALL, true));
        repository.setFeatureState(new FeatureState(TestFeature.ENABLED_FOR_CK, true)
            .setStrategyId(UsernameActivationStrategy.ID)
            .setParameter(UsernameActivationStrategy.PARAM_USERS, "ck"));
        return repository;
    }

    @Override
    public UserProvider getUserProvider() {
        return featureUserProvider;
    }

}
