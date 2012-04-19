package org.togglz.test.user.shiro;

import java.util.Arrays;

import javax.enterprise.context.ApplicationScoped;

import org.togglz.core.Feature;
import org.togglz.core.config.TogglzConfig;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.FeatureStateRepository;
import org.togglz.core.repository.mem.InMemoryRepository;
import org.togglz.core.user.FeatureUserProvider;
import org.togglz.shiro.ShiroFeatureUserProvider;
import org.togglz.test.user.UserDependentFeature;

@ApplicationScoped
public class ShiroUsersConfiguration implements TogglzConfig {

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
        return new ShiroFeatureUserProvider("togglz");
    }

}
