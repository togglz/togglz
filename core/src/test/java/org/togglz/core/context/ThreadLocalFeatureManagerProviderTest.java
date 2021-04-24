package org.togglz.core.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.user.NoOpUserProvider;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ThreadLocalFeatureManagerProviderTest {

    /**
     * Binds a FeatureManager to the thread before starting each test
     */
    @BeforeEach
    void before() {
        FeatureManager featureManager = new FeatureManagerBuilder()
                .featureEnum(MyFeature.class)
                .stateRepository(new InMemoryStateRepository())
                .userProvider(new NoOpUserProvider())
                .build();
        ThreadLocalFeatureManagerProvider.bind(featureManager);
    }

    /**
     * Release the FeatureManager after it
     */
    @AfterEach
    void after() {
        ThreadLocalFeatureManagerProvider.release();
    }

    /**
     * The first test requires a FeatureManager
     */
    @Test
    void firstTest() {
        FeatureManager featureManager = FeatureContext.getFeatureManager();
        assertNotNull(featureManager);
    }

    /**
     * The second test also
     */
    @Test
    void secondTest() {
        FeatureManager featureManager = FeatureContext.getFeatureManager();
        assertNotNull(featureManager);
    }

    private enum MyFeature implements Feature {
        FEATURE1,
        FEATURE2;
    }
}
