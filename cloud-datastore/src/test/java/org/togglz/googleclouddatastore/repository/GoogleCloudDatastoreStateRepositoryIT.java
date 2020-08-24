package org.togglz.googleclouddatastore.repository;

import com.google.cloud.datastore.BooleanValue;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.StringValue;
import com.google.cloud.datastore.Transaction;
import com.google.cloud.datastore.Value;
import com.google.cloud.datastore.testing.LocalDatastoreHelper;
import org.joda.time.Duration;
import org.junit.jupiter.api.*;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.togglz.googleclouddatastore.repository.GoogleCloudDatastoreStateRepository.KIND_DEFAULT;

public class GoogleCloudDatastoreStateRepositoryIT {

    private static final int MAX_ENTITY_GROUPS = 25;
    private static final LocalDatastoreHelper HELPER = LocalDatastoreHelper.create(1.0);
    private static final Datastore DATASTORE = HELPER.getOptions().getService();
    private static final String STRATEGY_ID = "myStrategy";

    private GoogleCloudDatastoreStateRepository repository;

    @BeforeAll
    public static void beforeClass() throws IOException, InterruptedException {
        HELPER.start();
    }

    @BeforeEach
    public void setUp() throws Exception {
        repository = new GoogleCloudDatastoreStateRepository(DATASTORE);
    }

    @AfterAll
    public static void afterClass() throws IOException, InterruptedException, TimeoutException {
        HELPER.stop(Duration.standardMinutes(1));
    }

    @AfterEach
    public void tearDown() throws Exception {
        HELPER.reset();
    }

    @Test
    public void shouldUseGiveKindWhenPersisting() {

        // GIVEN a repo with a custom kind
        final String kind = "CustomKind";
        repository = new GoogleCloudDatastoreStateRepository(DATASTORE, kind);

        // WHEN a feature is persisted
        final FeatureState state = new FeatureState(TestFeature.F1);
        repository.setFeatureState(state);

        // THEN new entities should be persisted within it
        final Key key = DATASTORE.newKeyFactory().setKind(kind).newKey(TestFeature.F1.name());
        final Entity entity = DATASTORE.get(key);
        assertNotNull(entity);
    }

    @Test
    public void testShouldSaveStateWithoutStrategyOrParameters() {
        //WHEN a feature without strategy is persisted
        final FeatureState state = new FeatureState(TestFeature.F1).disable();
        repository.setFeatureState(state);

        //THEN there should be a corresponding entry in the database
        final Key key = createKey(TestFeature.F1.name());
        final Entity featureEntity = DATASTORE.get(key);

        assertFalse(featureEntity.getBoolean(GoogleCloudDatastoreStateRepository.ENABLED));
        assertFalse(featureEntity.contains(GoogleCloudDatastoreStateRepository.STRATEGY_ID));
        assertFalse(featureEntity.contains(GoogleCloudDatastoreStateRepository.STRATEGY_PARAMS_NAMES));
        assertFalse(featureEntity.contains(GoogleCloudDatastoreStateRepository.STRATEGY_PARAMS_VALUES));

    }

    @Test
    public void testShouldSaveStateStrategyAndParameters() {

        // WHEN a feature without strategy is persisted
        final FeatureState state = new FeatureState(TestFeature.F1)
                .enable()
                .setStrategyId("someId")
                .setParameter("param", "foo");
        repository.setFeatureState(state);

        // THEN there should be a corresponding entry in the database
        final Key key = createKey(TestFeature.F1.name());
        final Entity featureEntity = DATASTORE.get(key);

        assertTrue(featureEntity.getBoolean(GoogleCloudDatastoreStateRepository.ENABLED));
        assertEquals("someId", featureEntity.getString(GoogleCloudDatastoreStateRepository.STRATEGY_ID));
        final StringValue param = NonIndexed.valueOf("param");
        assertEquals(singletonList(param), featureEntity.<StringValue>getList(GoogleCloudDatastoreStateRepository.STRATEGY_PARAMS_NAMES));
        final StringValue foo = NonIndexed.valueOf("foo");
        assertEquals(singletonList(foo), featureEntity.<StringValue>getList(GoogleCloudDatastoreStateRepository.STRATEGY_PARAMS_VALUES));
    }

    @Test
    public void shouldReturnNullWhenStateDoesntExist() {
        // GIVEN there is no feature state in the DATASTORE WHEN the repository reads the state
        final FeatureState state = repository.getFeatureState(TestFeature.F1);

        // THEN the properties should be set like expected
        assertNull(state);
    }

    @Test
    public void testShouldReadStateWithoutStrategyAndParameters() {

        // GIVEN a database row containing a simple feature state
        givenDisabledFeature("F1");

        // WHEN the repository reads the state
        final FeatureState state = repository.getFeatureState(TestFeature.F1);

        // THEN the properties should be set like expected
        assertNotNull(state);
        assertEquals(TestFeature.F1, state.getFeature());
        assertFalse(state.isEnabled());
        assertNull(state.getStrategyId());
        assertEquals(0, state.getParameterNames().size());
    }

    @Test
    public void testShouldReadStateWithStrategyAndParameters() {

        // GIVEN a database row containing a simple feature state
        givenEnabledFeatureWithStrategy("F1");

        // WHEN the repository reads the state
        final FeatureState state = repository.getFeatureState(TestFeature.F1);

        // THEN the properties should be set like expected
        assertNotNull(state);
        assertEquals(TestFeature.F1, state.getFeature());
        assertTrue(state.isEnabled());
        assertEquals(STRATEGY_ID, state.getStrategyId());
        assertEquals(1, state.getParameterNames().size());
        assertEquals("foobar", state.getParameter("param23"));
    }

    @Test
    public void testShouldUpdateExistingDatabaseEntry() {
        // GIVEN a database row containing a simple feature state
        givenEnabledFeatureWithStrategy("F1");

        // AND the database entries are like expected
        // THEN there should be a corresponding entry in the database
        final Key key = createKey(TestFeature.F1.name());
        Entity featureEntity = DATASTORE.get(key);

        assertTrue(featureEntity.getBoolean(GoogleCloudDatastoreStateRepository.ENABLED));
        assertEquals(STRATEGY_ID, featureEntity.getString(GoogleCloudDatastoreStateRepository.STRATEGY_ID));
        StringValue param = NonIndexed.valueOf("param23");
        assertEquals(singletonList(param), featureEntity.<StringValue>getList(GoogleCloudDatastoreStateRepository.STRATEGY_PARAMS_NAMES));
        StringValue foo = NonIndexed.valueOf("foobar");
        assertEquals(singletonList(foo), featureEntity.<StringValue>getList(GoogleCloudDatastoreStateRepository.STRATEGY_PARAMS_VALUES));

        // WHEN the repository writes new state
        final FeatureState state = new FeatureState(TestFeature.F1)
                .disable()
                .setStrategyId("someId")
                .setParameter("param", "foo");
        repository.setFeatureState(state);

        // THEN the properties should be set like expected
        featureEntity = DATASTORE.get(key);
        assertFalse(featureEntity.getBoolean(GoogleCloudDatastoreStateRepository.ENABLED));
        assertEquals("someId", featureEntity.getString(GoogleCloudDatastoreStateRepository.STRATEGY_ID));
        param = NonIndexed.valueOf("param");
        assertEquals(singletonList(param), featureEntity.<StringValue>getList(GoogleCloudDatastoreStateRepository.STRATEGY_PARAMS_NAMES));
        foo = NonIndexed.valueOf("foo");
        assertEquals(singletonList(foo), featureEntity.<StringValue>getList(GoogleCloudDatastoreStateRepository.STRATEGY_PARAMS_VALUES));
    }

    @Test
    public void shouldNotAddNewEntityGroupToCurrentCrossGroupTransaction() {
        givenDisabledFeature("F");
        final Transaction txn = DATASTORE.newTransaction();
        for (int i = 0; i < MAX_ENTITY_GROUPS - 1; i++) {
            putWithinTransaction("F" + i, false, txn);
        }
        putWithinTransaction("F", false, txn);
        repository.getFeatureState(TestFeature.F1);
        txn.commit();
    }

    @Test
    public void shouldWorkInsideRunningTransaction() {
        givenDisabledFeature("F1");
        final Transaction txn = DATASTORE.newTransaction();
        putWithinTransaction("F3", false, txn);
        repository.getFeatureState(TestFeature.F1);
        txn.commit();
    }

    private Key createKey(String name) {
        return DATASTORE.newKeyFactory().setKind(KIND_DEFAULT).newKey(name);
    }

    private void givenDisabledFeature(String featureName) {
        put(featureName, false, null, null, null);
    }

    private void givenEnabledFeatureWithStrategy(String featureName) {
        put(featureName, true, STRATEGY_ID, new HashMap<String, String>() {{
            put("param23", "foobar");
        }});
    }

    private void putWithinTransaction(final String name, final boolean enabled, final Transaction txn) {
        put(name, enabled, null, null, txn);
    }

    private void put(final String name, final boolean enabled, final String strategyId, final Map<String, String> params) {
        put(name, enabled, strategyId, params, null);
    }

    private void put(final String name, final boolean enabled, final String strategyId, final Map<String, String> params,
                        final Transaction txn) {

        final Key key = createKey(name);
        final Entity.Builder builder = Entity.newBuilder(key)
                .set(GoogleCloudDatastoreStateRepository.ENABLED, BooleanValue.newBuilder(enabled).setExcludeFromIndexes(true).build());

        if (strategyId != null) {
            builder.set(GoogleCloudDatastoreStateRepository.STRATEGY_ID, StringValue.newBuilder(strategyId).setExcludeFromIndexes(true).build());
        }

        if (params != null && !params.isEmpty()) {
            final List<Value<String>> strategyParamsNames = new ArrayList<>(params.size());
            final List<Value<String>> strategyParamsValues = new ArrayList<>(params.size());
            for (final String paramName : params.keySet()) {
                strategyParamsNames.add(StringValue.newBuilder(paramName).setExcludeFromIndexes(true).build());
                strategyParamsValues.add(StringValue.newBuilder(params.get(paramName)).setExcludeFromIndexes(true).build());
            }
            builder.set(GoogleCloudDatastoreStateRepository.STRATEGY_PARAMS_NAMES, strategyParamsNames);
            builder.set(GoogleCloudDatastoreStateRepository.STRATEGY_PARAMS_VALUES, strategyParamsValues);
        }

        if (txn == null) {
            DATASTORE.put(builder.build());
        } else {
            txn.put(builder.build());
        }
    }

    private enum TestFeature implements Feature {
        F1
    }

}