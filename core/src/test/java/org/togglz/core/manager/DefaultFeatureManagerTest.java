package org.togglz.core.manager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.togglz.core.Feature;
import org.togglz.core.activation.UsernameActivationStrategy;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.spi.FeatureProvider;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultFeatureManagerTest {

    private StateRepository repository;
    private FeatureManager manager;
    private TestFeatureUserProvider featureUserProvider;

    @BeforeEach
    void before() {
        repository = new InMemoryStateRepository();
        repository.setFeatureState(new FeatureState(MyFeatures.DELETE_USERS, true)
            .setStrategyId(UsernameActivationStrategy.ID)
            .setParameter(UsernameActivationStrategy.PARAM_USERS, "admin"));
        repository.setFeatureState(new FeatureState(MyFeatures.MISSING_STRATEGY, true)
            .setStrategyId("NoSuchActivationStrategy"));
        repository.setFeatureState(new FeatureState(MyFeatures.EXPERIMENTAL, false));
        repository.setFeatureState(new FeatureState(MyFeatures.EMPTY_STRATEGY, true)
            .setStrategyId(""));

        featureUserProvider = new TestFeatureUserProvider();

        manager = new FeatureManagerBuilder()
            .featureEnum(MyFeatures.class)
            .stateRepository(repository)
            .userProvider(featureUserProvider)
            .build();

    }

    @AfterEach
    void after() {
        repository = null;
        manager = null;
        featureUserProvider = null;
    }

    @Test
    void testGetFeatures() {
        assertTrue(manager.getFeatures().containsAll(Arrays.asList(MyFeatures.DELETE_USERS, MyFeatures.EXPERIMENTAL, MyFeatures.MISSING_STRATEGY)));
    }

    @Test
    void testIsActive() {
        // DELETE_USERS disabled for unknown user
        featureUserProvider.setFeatureUser(null);
        assertFalse(manager.isActive(MyFeatures.DELETE_USERS));

        // DELETE_USERS enabled for admin user
        featureUserProvider.setFeatureUser(new SimpleFeatureUser("admin", false));
        assertTrue(manager.isActive(MyFeatures.DELETE_USERS));

        // DELETE_USERS enabled for other user
        featureUserProvider.setFeatureUser(new SimpleFeatureUser("somebody", false));
        assertFalse(manager.isActive(MyFeatures.DELETE_USERS));

        // EXPERIMENTAL disabled for all
        featureUserProvider.setFeatureUser(null);
        assertFalse(manager.isActive(MyFeatures.EXPERIMENTAL));

        // MISSING_STRATEGY disabled for all
        assertFalse(manager.isActive(MyFeatures.MISSING_STRATEGY));

        // EMPTY_STRATEGY enabled for all
        assertTrue(manager.isActive(MyFeatures.EMPTY_STRATEGY));
    }

    @Test
    void testIsActiveUsingDefaultFeatureState() {
        FeatureProvider featureProvider = mock(FeatureProvider.class);
        FeatureMetaData featureMetaData = mock(FeatureMetaData.class);
        when(featureMetaData.getDefaultFeatureState()).thenReturn(new FeatureState(MyFeatures.NOT_STORED_FEATURE, true));
        when(featureProvider.getMetaData(MyFeatures.NOT_STORED_FEATURE)).thenReturn(featureMetaData);

        FeatureManager manager = new FeatureManagerBuilder()
            .featureEnum(MyFeatures.class)
            .stateRepository(repository)
            .featureProvider(featureProvider)
            .userProvider(featureUserProvider)
            .build();

        assertTrue(manager.isActive(MyFeatures.NOT_STORED_FEATURE));

    }

    @Test
    void testShouldHandleEnabledFlagCorrectlyWithCustomStrategy() {
        // enabled for admin
        featureUserProvider.setFeatureUser(new SimpleFeatureUser("admin", false));
        assertTrue(manager.isActive(MyFeatures.DELETE_USERS));

        // disable feature, but keep configuration
        FeatureState state = repository.getFeatureState(MyFeatures.DELETE_USERS);
        state.setEnabled(false);
        repository.setFeatureState(state);

        // enabled for admin
        assertFalse(manager.isActive(MyFeatures.DELETE_USERS));

    }

    @Test
    void testGetFeatureState() {
        FeatureState state = manager.getFeatureState(MyFeatures.DELETE_USERS);
        assertEquals(MyFeatures.DELETE_USERS, state.getFeature());
        assertTrue(state.isEnabled());
        assertEquals("admin", state.getParameter(UsernameActivationStrategy.PARAM_USERS));

    }

    @Test
    void testGetFeatureStateUsingDefaultFeatureState() {
        FeatureProvider featureProvider = mock(FeatureProvider.class);
        FeatureMetaData featureMetaData = mock(FeatureMetaData.class);
        when(featureMetaData.getDefaultFeatureState()).thenReturn(new FeatureState(MyFeatures.NOT_STORED_FEATURE, true));
        when(featureProvider.getMetaData(MyFeatures.NOT_STORED_FEATURE)).thenReturn(featureMetaData);

        FeatureManager manager = new FeatureManagerBuilder()
            .featureEnum(MyFeatures.class)
            .stateRepository(repository)
            .featureProvider(featureProvider)
            .userProvider(featureUserProvider)
            .build();


        FeatureState state = manager.getFeatureState(MyFeatures.NOT_STORED_FEATURE);
        assertEquals(MyFeatures.NOT_STORED_FEATURE, state.getFeature());
        assertTrue(state.isEnabled());

    }

    /**
     * {@link UserProvider} that allows to set the user directly
     */
    private static final class TestFeatureUserProvider implements UserProvider {

        private FeatureUser featureUser;

        void setFeatureUser(FeatureUser featureUser) {
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
    private enum MyFeatures implements Feature {
        DELETE_USERS,
        EXPERIMENTAL,
        MISSING_STRATEGY,
        EMPTY_STRATEGY,
        NOT_STORED_FEATURE
    }
}
