package org.togglz.shiro.test;

import org.togglz.core.Feature;
import org.togglz.core.activation.UsernameActivationStrategy;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.user.UserProvider;
import org.togglz.shiro.ShiroUserProvider;

public class ShiroUsersConfiguration implements TogglzConfig {

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
        return new ShiroUserProvider("togglz");
    }

}
