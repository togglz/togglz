package org.togglz.core.manager;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;

public class DefaultFeatureManagerTest {

    private StateRepository repository;
    private FeatureManager manager;
    private TestFeatureUserProvider featureUserProvider;

    @Before
    public void before() {

        repository = new InMemoryStateRepository();
        repository.setFeatureState(new FeatureState(MyFeatures.DELETE_USERS, true, Arrays.asList("admin")));
        repository.setFeatureState(new FeatureState(MyFeatures.EXPERIMENTAL, false));

        featureUserProvider = new TestFeatureUserProvider();

        manager = new FeatureManagerBuilder()
                .featureClass(MyFeatures.class)
                .stateRepository(repository)
                .userProvider(featureUserProvider)
                .build();

    }

    @After
    public void after() {
        repository = null;
        manager = null;
        featureUserProvider = null;
    }

    @Test
    public void testGetFeatures() {
        assertEquals(2, manager.getFeatures().length);
        assertEquals(MyFeatures.DELETE_USERS, manager.getFeatures()[0]);
        assertEquals(MyFeatures.EXPERIMENTAL, manager.getFeatures()[1]);
    }

    @Test
    public void testIsActive() {

        // DELETE_USERS disabled for unknown user
        featureUserProvider.setFeatureUser(null);
        assertEquals(false, manager.isActive(MyFeatures.DELETE_USERS));

        // DELETE_USERS enabled for admin user
        featureUserProvider.setFeatureUser(new SimpleFeatureUser("admin", false));
        assertEquals(true, manager.isActive(MyFeatures.DELETE_USERS));

        // DELETE_USERS enabled for other user
        featureUserProvider.setFeatureUser(new SimpleFeatureUser("somebody", false));
        assertEquals(false, manager.isActive(MyFeatures.DELETE_USERS));

        // EXPERIMENTAL disabled for all
        featureUserProvider.setFeatureUser(null);
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
     * {@link UserProvider} that allows to set the user directly
     */
    private final class TestFeatureUserProvider implements UserProvider {

        private FeatureUser featureUser;

        public void setFeatureUser(FeatureUser featureUser) {
            this.featureUser = featureUser;
        }

        @Override
        public FeatureUser getCurrentUser() {
            return featureUser;
        }

    }

    /**
     * Feature under test
     */
    private static enum MyFeatures implements Feature {

        @EnabledByDefault
        DELETE_USERS,

        EXPERIMENTAL;

        @Override
        public boolean isActive() {
            return FeatureContext.getFeatureManager().isActive(this);
        }

    }

}
