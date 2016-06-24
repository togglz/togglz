package org.togglz.appengine.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class DatastoreStateRepositoryTest {

    public static final int MAX_ENTITY_GROUPS = 25;
    private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
        new LocalDatastoreServiceTestConfig().setApplyAllHighRepJobPolicy());

    private DatastoreStateRepository repository;
    private DatastoreService datastoreService;

    @Before
    public void setup() {
        helper.setUp();
        datastoreService = DatastoreServiceFactory.getDatastoreService();
        repository = new DatastoreStateRepository(datastoreService);
    }

    @After
    public void tearDown() {
        helper.tearDown();
    }

    @Test
    public void customKindName() throws EntityNotFoundException {
        final String kind = "CustomKind";
        repository = new DatastoreStateRepository(kind, datastoreService);
        assertEquals(kind, repository.kind());
    }

    @Test
    public void shouldNotAddNewEntityGroupToCurrentCrossGroupTransaction() {
        update("F", false, null, null, null);
        final Transaction txn = datastoreService.beginTransaction( TransactionOptions.Builder.withXG(true));
        for (int i = 0; i < MAX_ENTITY_GROUPS - 1; i++) {
            update("F" + i, false, null, null, txn);
        }
        update("F", false, null, null, txn);
        repository.getFeatureState(TestFeature.F1);
        txn.commit();
    }

    @Test
    public void shouldNotStartNewTransaction() {
        update("F1", false, null, null, null);
        DatastoreService spyDatastoreService = Mockito.spy(DelegateDatastoreService.getInstance(datastoreService));
        repository = new DatastoreStateRepository(spyDatastoreService);
        repository.getFeatureState(TestFeature.F1);
        Mockito.verify(spyDatastoreService, Mockito.never()).beginTransaction();
    }

    @Test
    public void shouldWorkInsideRunningTransaction() {
        update("F1", false, null, null, null);
        final Transaction txn = datastoreService.beginTransaction();
        update("F3", false, null, null, txn);
        repository.getFeatureState(TestFeature.F1);
        txn.commit();
    }

    @Test
    public void testShouldSaveStateWithoutStrategyOrParameters() throws EntityNotFoundException {
        /*
         * WHEN a feature without strategy is persisted
         */
        final FeatureState state = new FeatureState(TestFeature.F1).disable();
        repository.setFeatureState(state);

        /*
         * THEN there should be a corresponding entry in the database
         */
        final Key key = KeyFactory.createKey(repository.kind(), TestFeature.F1.name());
        final Entity featureEntity = datastoreService.get(key);

        assertEquals(false, featureEntity.getProperty(DatastoreStateRepository.ENABLED));
        assertNull(featureEntity.getProperty(DatastoreStateRepository.STRATEGY_ID));
        assertNull(featureEntity.getProperty(DatastoreStateRepository.STRATEGY_PARAMS_NAMES));
        assertNull(featureEntity.getProperty(DatastoreStateRepository.STRATEGY_PARAMS_VALUES));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testShouldSaveStateStrategyAndParameters() throws EntityNotFoundException {

        /*
         * WHEN a feature without strategy is persisted
         */
        final FeatureState state = new FeatureState(TestFeature.F1)
            .enable()
            .setStrategyId("someId")
            .setParameter("param", "foo");
        repository.setFeatureState(state);

        /*
         * THEN there should be a corresponding entry in the database
         */
        final Key key = KeyFactory.createKey(repository.kind(), TestFeature.F1.name());
        final Entity featureEntity = datastoreService.get(key);

        assertEquals(true, featureEntity.getProperty(DatastoreStateRepository.ENABLED));
        assertEquals("someId", featureEntity.getProperty(DatastoreStateRepository.STRATEGY_ID));
        assertThat((List<String>) featureEntity.getProperty(DatastoreStateRepository.STRATEGY_PARAMS_NAMES),
            is(Arrays.asList("param")));
        assertThat((List<String>) featureEntity.getProperty(DatastoreStateRepository.STRATEGY_PARAMS_VALUES),
            is(Arrays.asList("foo")));
    }

    @Test
    public void shouldReturnNullWhenStateDoesntExist() {
        /*
         * GIVEN there is no feature state in the datastore WHEN the repository reads the state
         */
        final FeatureState state = repository.getFeatureState(TestFeature.F1);

        /*
         * THEN the properties should be set like expected
         */
        assertNull(state);
    }

    @Test
    public void testShouldReadStateWithoutStrategyAndParameters() {

        /*
         * GIVEN a database row containing a simple feature state
         */
        update("F1", false, null, null, null);

        /*
         * WHEN the repository reads the state
         */
        final FeatureState state = repository.getFeatureState(TestFeature.F1);

        /*
         * THEN the properties should be set like expected
         */
        assertNotNull(state);
        assertEquals(TestFeature.F1, state.getFeature());
        assertEquals(false, state.isEnabled());
        assertEquals(null, state.getStrategyId());
        assertEquals(0, state.getParameterNames().size());

    }

    @SuppressWarnings("serial")
    @Test
    public void testShouldReadStateWithStrategyAndParameters() {

        /*
         * GIVEN a database row containing a simple feature state
         */
        final Map<String, String> map = new HashMap<String, String>() {
            {
                put("param23", "foobar");
            }
        };

        update("F1", true, "myStrategy", map, null);

        /*
         * WHEN the repository reads the state
         */
        final FeatureState state = repository.getFeatureState(TestFeature.F1);

        /*
         * THEN the properties should be set like expected
         */
        assertNotNull(state);
        assertEquals(TestFeature.F1, state.getFeature());
        assertEquals(true, state.isEnabled());
        assertEquals("myStrategy", state.getStrategyId());
        assertEquals(1, state.getParameterNames().size());
        assertEquals("foobar", state.getParameter("param23"));

    }

    @SuppressWarnings({ "unchecked", "serial" })
    @Test
    public void testShouldUpdateExistingDatabaseEntry() throws EntityNotFoundException {

        /*
         * GIVEN a database row containing a simple feature state
         */
        final Map<String, String> map = new HashMap<String, String>() {
            {
                put("param23", "foobar");
            }
        };
        update("F1", true, "myStrategy", map, null);

        /*
         * AND the database entries are like expected
         */
        /*
         * THEN there should be a corresponding entry in the database
         */
        final Key key = KeyFactory.createKey(repository.kind(), TestFeature.F1.name());
        Entity featureEntity = datastoreService.get(key);

        assertEquals(true, featureEntity.getProperty(DatastoreStateRepository.ENABLED));
        assertEquals("myStrategy", featureEntity.getProperty(DatastoreStateRepository.STRATEGY_ID));
        assertThat((List<String>) featureEntity.getProperty(DatastoreStateRepository.STRATEGY_PARAMS_NAMES),
            is(Arrays.asList("param23")));
        assertThat((List<String>) featureEntity.getProperty(DatastoreStateRepository.STRATEGY_PARAMS_VALUES),
            is(Arrays.asList("foobar")));

        /*
         * WHEN the repository writes new state
         */
        final FeatureState state = new FeatureState(TestFeature.F1)
            .disable()
            .setStrategyId("someId")
            .setParameter("param", "foo");
        repository.setFeatureState(state);

        /*
         * THEN the properties should be set like expected
         */
        featureEntity = datastoreService.get(key);

        assertEquals(false, featureEntity.getProperty(DatastoreStateRepository.ENABLED));
        assertEquals("someId", featureEntity.getProperty(DatastoreStateRepository.STRATEGY_ID));
        assertThat((List<String>) featureEntity.getProperty(DatastoreStateRepository.STRATEGY_PARAMS_NAMES),
            is(Arrays.asList("param")));
        assertThat((List<String>) featureEntity.getProperty(DatastoreStateRepository.STRATEGY_PARAMS_VALUES),
            is(Arrays.asList("foo")));
    }

    private void update(final String name, final boolean enabled, final String strategyId, final Map<String, String> params,
        final Transaction txn) {
        final Entity featureEntity = new Entity(repository.kind(), name);
        featureEntity.setUnindexedProperty(DatastoreStateRepository.ENABLED, enabled);
        featureEntity.setUnindexedProperty(DatastoreStateRepository.STRATEGY_ID, strategyId);

        if (params != null) {
            final List<String> strategyParamsNames = new ArrayList<String>();
            final List<String> strategyParamsValues = new ArrayList<String>();
            for (final String paramName : params.keySet()) {
                strategyParamsNames.add(paramName);
                strategyParamsValues.add(params.get(paramName));
            }
            featureEntity.setUnindexedProperty(DatastoreStateRepository.STRATEGY_PARAMS_NAMES, strategyParamsNames);
            featureEntity.setUnindexedProperty(DatastoreStateRepository.STRATEGY_PARAMS_VALUES, strategyParamsValues);
        }

        if (txn == null) {
            datastoreService.put(featureEntity);
        } else {
            datastoreService.put(txn, featureEntity);
        }
    }

    private static enum TestFeature implements Feature {
        F1
    }
}