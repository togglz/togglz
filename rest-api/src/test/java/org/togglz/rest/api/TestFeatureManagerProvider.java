package org.togglz.rest.api;

import java.util.List;

import org.togglz.core.activation.ActivationStrategyProvider;
import org.togglz.core.activation.UsernameActivationStrategy;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.spi.FeatureManagerProvider;
import org.togglz.core.user.NoOpUserProvider;
import org.togglz.rest.api.TogglzRestApiServletTest.TestFeatures;

import com.google.common.collect.ImmutableList;

public class TestFeatureManagerProvider implements FeatureManagerProvider {

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public FeatureManager getFeatureManager() {
        return FeatureManagerBuilder.begin()
            .featureEnum(TestFeatures.class)
            .activationStrategyProvider(new ActivationStrategyProvider() {
                @Override
                public List<ActivationStrategy> getActivationStrategies() {
                    return ImmutableList.<ActivationStrategy>of(
                        new UsernameActivationStrategy()
                    );
                }
            })
            .stateRepository(new InMemoryStateRepository())
            .userProvider(new NoOpUserProvider())
            .build();
    }
    
}