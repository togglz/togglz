package org.togglz.core.context;

import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.user.NoOpUserProvider;

public class ThreadLocalFeatureManagerProviderTest {

    /**
     * Binds a FeatureManager to the thread before starting each test
     */
    @Before
    public void before() {
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
    @After
    public void after() {
        ThreadLocalFeatureManagerProvider.release();
    }

    /**
     * The first test requires a FeatureManager
     */
    @Test
    public void firstTest() {
        FeatureManager featureManager = FeatureContext.getFeatureManager();
        assertNotNull(featureManager);
    }

    /**
     * The second test also
     */
    @Test
    public void secondTest() {
        FeatureManager featureManager = FeatureContext.getFeatureManager();
        assertNotNull(featureManager);
    }

    private static enum MyFeature implements Feature {
        FEATURE1,
        FEATURE2;
    }

}
