package org.togglz.core.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
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

public class DefaultFeatureManagerTest {

    private StateRepository repository;
    private FeatureManager manager;
    private TestFeatureUserProvider featureUserProvider;

    @Before
    public void before() {

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

    @After
    public void after() {
        repository = null;
        manager = null;
        featureUserProvider = null;
    }

    @Test
    public void testGetFeatures() {
        assertThat(manager.getFeatures())
            .contains(MyFeatures.DELETE_USERS, MyFeatures.EXPERIMENTAL, MyFeatures.MISSING_STRATEGY);
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

        // MISSING_STRATEGY disabled for all
        assertEquals(false, manager.isActive(MyFeatures.MISSING_STRATEGY));

        // EMPTY_STRATEGY enabled for all
        assertEquals(true, manager.isActive(MyFeatures.EMPTY_STRATEGY));
    }

    @Test
    public void testIsActiveUsingDefaultFeatureState() {
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

        assertEquals(true, manager.isActive(MyFeatures.NOT_STORED_FEATURE));

    }

    @Test
    public void testShouldHandleEnabledFlagCorrectlyWithCustomStrategy() {

        // enabled for admin
        featureUserProvider.setFeatureUser(new SimpleFeatureUser("admin", false));
        assertEquals(true, manager.isActive(MyFeatures.DELETE_USERS));

        // disable feature, but keep configuration
        FeatureState state = repository.getFeatureState(MyFeatures.DELETE_USERS);
        state.setEnabled(false);
        repository.setFeatureState(state);

        // enabled for admin
        assertEquals(false, manager.isActive(MyFeatures.DELETE_USERS));

    }

    @Test
    public void testGetFeatureState() {

        FeatureState state = manager.getFeatureState(MyFeatures.DELETE_USERS);
        assertEquals(MyFeatures.DELETE_USERS, state.getFeature());
        assertEquals(true, state.isEnabled());
        assertEquals("admin", state.getParameter(UsernameActivationStrategy.PARAM_USERS));

    }

    @Test
    public void testGetFeatureStateUsingDefaultFeatureState() {
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
        assertEquals(true, state.isEnabled());

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
        DELETE_USERS,
        EXPERIMENTAL,
        MISSING_STRATEGY,
        EMPTY_STRATEGY,
        NOT_STORED_FEATURE;
    }

}
