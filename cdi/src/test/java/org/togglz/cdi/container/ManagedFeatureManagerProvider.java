package org.togglz.cdi.container;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.togglz.cdi.Features;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.user.NoOpUserProvider;

@ApplicationScoped
public class ManagedFeatureManagerProvider {

    @Produces
    public FeatureManager produce() {
        return new FeatureManagerBuilder()
            .featureEnum(Features.class)
            .userProvider(new NoOpUserProvider())
            .stateRepository(new InMemoryStateRepository())
            .name("I'm managed by CDI")
            .build();
    }

}
