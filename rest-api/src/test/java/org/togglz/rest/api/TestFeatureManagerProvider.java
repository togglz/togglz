package org.togglz.rest.api;

import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.spi.FeatureManagerProvider;
import org.togglz.core.user.NoOpUserProvider;
import org.togglz.rest.api.TogglzRestApiServletTest.TestFeatures;

public class TestFeatureManagerProvider implements FeatureManagerProvider {

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public FeatureManager getFeatureManager() {
        return FeatureManagerBuilder.begin()
            .featureEnum(TestFeatures.class)
            .stateRepository(new InMemoryStateRepository())
            .userProvider(new NoOpUserProvider())
            .build();
    }
    
}