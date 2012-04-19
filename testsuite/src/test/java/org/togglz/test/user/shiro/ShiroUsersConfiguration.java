package org.togglz.test.user.shiro;

import java.util.Arrays;

import javax.enterprise.context.ApplicationScoped;

import org.togglz.core.Feature;
import org.togglz.core.config.TogglzConfig;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.user.UserProvider;
import org.togglz.shiro.ShiroUserProvider;
import org.togglz.test.user.UserDependentFeature;

@ApplicationScoped
public class ShiroUsersConfiguration implements TogglzConfig {

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
        return new ShiroUserProvider("togglz");
    }

}
