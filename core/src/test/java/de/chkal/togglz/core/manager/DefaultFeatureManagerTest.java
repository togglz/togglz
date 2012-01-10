package de.chkal.togglz.core.manager;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.chkal.togglz.core.Feature;
import de.chkal.togglz.core.config.FeatureManagerConfiguration;
import de.chkal.togglz.core.repository.FeatureStateRepository;
import de.chkal.togglz.core.repository.mem.InMemoryRepository;
import de.chkal.togglz.core.user.FeatureUserProvider;
import de.chkal.togglz.core.user.NoOpFeatureUserProvider;

public class DefaultFeatureManagerTest {

    private FeatureStateRepository repository;
    private FeatureManager manager;

    @Before
    public void before() {
        repository = new InMemoryRepository();
        repository.setFeatureState(new FeatureState(MyFeatures.DELETE_USERS, true, Arrays.asList("admin")));
        repository.setFeatureState(new FeatureState(MyFeatures.EXPERIMENTAL, false));
        manager = new DefaultFeatureManager(new MyConfiguration());
    }

    @After
    public void after() {
        repository = null;
        manager = null;
    }

    @Test
    public void testGetFeatures() {
        assertEquals(2, manager.getFeatures().length);
        assertEquals(MyFeatures.DELETE_USERS, manager.getFeatures()[0]);
        assertEquals(MyFeatures.EXPERIMENTAL, manager.getFeatures()[1]);
    }

    @Test
    public void testIsActive() {

        // DELETE_USERS only active for admin
        assertEquals(false, manager.isActive(MyFeatures.DELETE_USERS));
        assertEquals(false, manager.isActive(MyFeatures.DELETE_USERS));
        assertEquals(true, manager.isActive(MyFeatures.DELETE_USERS));

        // EXPERIMENTAL disabled for all
        assertEquals(false, manager.isActive(MyFeatures.EXPERIMENTAL));

    }

    @Test
    public void testGetFeatureState() {

        FeatureState state = manager.getFeatureState(MyFeatures.DELETE_USERS);
        assertEquals(MyFeatures.DELETE_USERS, state.getFeature());
        assertEquals(true, state.isEnabled());
        assertEquals(Arrays.asList("admin"), state.getUsers());

    }

    /**
     * Configuration for the {@link FeatureManager}
     */
    private final class MyConfiguration implements FeatureManagerConfiguration {

        public Class<? extends Feature> getFeatureClass() {
            return MyFeatures.class;
        }

        public FeatureStateRepository getFeatureStateRepository() {
            return repository;
        }

        @Override
        public FeatureUserProvider getFeatureUserProvider() {
            return new NoOpFeatureUserProvider();
        }

    }

    /**
     * Feature under test
     */
    private static enum MyFeatures implements Feature {
        
        DELETE_USERS, EXPERIMENTAL;

    }

}
