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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.togglz.googleclouddatastore.repository.GoogleCloudDatastoreStateRepository.KIND_DEFAULT;

public class GoogleCloudDatastoreStateRepositoryTest {

    private static final int MAX_ENTITY_GROUPS = 25;
    private static final LocalDatastoreHelper HELPER = LocalDatastoreHelper.create(1.0);
    private static final Datastore DATASTORE = HELPER.getOptions().getService();

    private GoogleCloudDatastoreStateRepository repository;

    @BeforeClass
    public static void beforeClass() throws IOException, InterruptedException {
        HELPER.start();
    }

    @Before
    public void setUp() throws Exception {
        repository = new GoogleCloudDatastoreStateRepository(DATASTORE);
    }

    @AfterClass
    public static void afterClass() throws IOException, InterruptedException, TimeoutException {
        HELPER.stop(Duration.standardMinutes(1));
    }

    @After
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

        assertEquals(false, featureEntity.getBoolean(GoogleCloudDatastoreStateRepository.ENABLED));
        assertFalse(featureEntity.contains(GoogleCloudDatastoreStateRepository.STRATEGY_ID));
        assertFalse(featureEntity.contains(GoogleCloudDatastoreStateRepository.STRATEGY_PARAMS_NAMES));
        assertFalse(featureEntity.contains(GoogleCloudDatastoreStateRepository.STRATEGY_PARAMS_VALUES));

    }

    @SuppressWarnings("unchecked")
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

        assertEquals(true, featureEntity.getBoolean(GoogleCloudDatastoreStateRepository.ENABLED));
        assertEquals("someId", featureEntity.getString(GoogleCloudDatastoreStateRepository.STRATEGY_ID));
        final StringValue param = StringValue.newBuilder("param").setExcludeFromIndexes(true).build();
        assertThat(featureEntity.<StringValue>getList(GoogleCloudDatastoreStateRepository.STRATEGY_PARAMS_NAMES),
                is(Arrays.asList(param)));
        final StringValue foo = StringValue.newBuilder("foo").setExcludeFromIndexes(true).build();
        assertThat(featureEntity.<StringValue>getList(GoogleCloudDatastoreStateRepository.STRATEGY_PARAMS_VALUES),
                is(Arrays.asList(foo)));
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
        update("F1", false, null, null, null);

        // WHEN the repository reads the state
        final FeatureState state = repository.getFeatureState(TestFeature.F1);

        // THEN the properties should be set like expected
        assertNotNull(state);
        assertEquals(TestFeature.F1, state.getFeature());
        assertEquals(false, state.isEnabled());
        assertEquals(null, state.getStrategyId());
        assertEquals(0, state.getParameterNames().size());

    }

    @SuppressWarnings("serial")
    @Test
    public void testShouldReadStateWithStrategyAndParameters() {

        // GIVEN a database row containing a simple feature state
        final Map<String, String> map = new HashMap<String, String>() {
            {
                put("param23", "foobar");
            }
        };

        update("F1", true, "myStrategy", map, null);

        // WHEN the repository reads the state
        final FeatureState state = repository.getFeatureState(TestFeature.F1);

        // THEN the properties should be set like expected
        assertNotNull(state);
        assertEquals(TestFeature.F1, state.getFeature());
        assertEquals(true, state.isEnabled());
        assertEquals("myStrategy", state.getStrategyId());
        assertEquals(1, state.getParameterNames().size());
        assertEquals("foobar", state.getParameter("param23"));

    }

    @SuppressWarnings({ "unchecked", "serial" })
    @Test
    public void testShouldUpdateExistingDatabaseEntry() {

        // GIVEN a database row containing a simple feature state
        final Map<String, String> map = new HashMap<String, String>() {
            {
                put("param23", "foobar");
            }
        };
        update("F1", true, "myStrategy", map, null);

        // AND the database entries are like expected
        // THEN there should be a corresponding entry in the database
        final Key key = createKey(TestFeature.F1.name());
        Entity featureEntity = DATASTORE.get(key);

        assertEquals(true, featureEntity.getBoolean(GoogleCloudDatastoreStateRepository.ENABLED));
        assertEquals("myStrategy", featureEntity.getString(GoogleCloudDatastoreStateRepository.STRATEGY_ID));
        StringValue param = StringValue.newBuilder("param23").setExcludeFromIndexes(true).build();
        assertThat(featureEntity.<StringValue>getList(GoogleCloudDatastoreStateRepository.STRATEGY_PARAMS_NAMES),
                is(Arrays.asList(param)));
        StringValue foo = StringValue.newBuilder("foobar").setExcludeFromIndexes(true).build();
        assertThat(featureEntity.<StringValue>getList(GoogleCloudDatastoreStateRepository.STRATEGY_PARAMS_VALUES),
                is(Arrays.asList(foo)));

        // WHEN the repository writes new state
        final FeatureState state = new FeatureState(TestFeature.F1)
                .disable()
                .setStrategyId("someId")
                .setParameter("param", "foo");
        repository.setFeatureState(state);

        // THEN the properties should be set like expected
        featureEntity = DATASTORE.get(key);
        assertEquals(false, featureEntity.getBoolean(GoogleCloudDatastoreStateRepository.ENABLED));
        assertEquals("someId", featureEntity.getString(GoogleCloudDatastoreStateRepository.STRATEGY_ID));
        param = StringValue.newBuilder("param").setExcludeFromIndexes(true).build();
        assertThat(featureEntity.<StringValue>getList(GoogleCloudDatastoreStateRepository.STRATEGY_PARAMS_NAMES),
                is(Arrays.asList(param)));
        foo = StringValue.newBuilder("foo").setExcludeFromIndexes(true).build();
        assertThat(featureEntity.<StringValue>getList(GoogleCloudDatastoreStateRepository.STRATEGY_PARAMS_VALUES),
                is(Arrays.asList(foo)));

    }

    private Key createKey(String name) {
        return DATASTORE.newKeyFactory().setKind(KIND_DEFAULT).newKey(name);
    }

    @Test
    public void shouldNotAddNewEntityGroupToCurrentCrossGroupTransaction() {
        update("F", false, null, null, null);
        final Transaction txn = DATASTORE.newTransaction();
        for (int i = 0; i < MAX_ENTITY_GROUPS - 1; i++) {
            update("F" + i, false, null, null, txn);
        }
        update("F", false, null, null, txn);
        repository.getFeatureState(TestFeature.F1);
        txn.commit();
    }

    @Test
    public void shouldWorkInsideRunningTransaction() {
        update("F1", false, null, null, null);
        final Transaction txn = DATASTORE.newTransaction();
        update("F3", false, null, null, txn);
        repository.getFeatureState(TestFeature.F1);
        txn.commit();
    }

    private void update(final String name, final boolean enabled, final String strategyId, final Map<String, String> params,
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