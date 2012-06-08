package org.togglz.core.context;

import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.config.TogglzConfig;
import org.togglz.core.manager.DefaultFeatureManager;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.user.NoOpUserProvider;
import org.togglz.core.user.UserProvider;

public class ThreadLocalFeatureManagerProviderTest {

    /**
     * Binds a FeatureManager to the thread before starting each test
     */
    @Before
    public void before() {
        FeatureManager featureManager = new DefaultFeatureManager(new DummyConfig());
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

    private static class DummyConfig implements TogglzConfig {

        @Override
        public Class<? extends Feature> getFeatureClass() {
            return Feature.class;
        }

        @Override
        public StateRepository getStateRepository() {
            return new InMemoryStateRepository();
        }

        @Override
        public UserProvider getUserProvider() {
            return new NoOpUserProvider();
        }

    }

}
