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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class DatastoreStateRepositoryTest {

	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
			new LocalDatastoreServiceTestConfig());

	private DatastoreStateRepository repository;
	private DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
	
	@Before
	public void setup() {
		helper.setUp();
		repository = new DatastoreStateRepository(datastoreService);
	}

	@After
	public void tearDown() {
		helper.tearDown();
	}
	
    @Test
    public void testShouldSaveStateWithoutStrategyOrParameters() throws EntityNotFoundException  {
        /*
         * WHEN a feature without strategy is persisted
         */
        FeatureState state = new FeatureState(TestFeature.F1).disable();
        repository.setFeatureState(state);

        /*
         * THEN there should be a corresponding entry in the database
         */
		Key key = KeyFactory.createKey(repository.kind(), TestFeature.F1.name());
		Entity featureEntity = datastoreService.get(key);
		
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
        FeatureState state = new FeatureState(TestFeature.F1)
            .enable()
            .setStrategyId("someId")
            .setParameter("param", "foo");
        repository.setFeatureState(state);

        /*
         * THEN there should be a corresponding entry in the database
         */
		Key key = KeyFactory.createKey(repository.kind(), TestFeature.F1.name());
		Entity featureEntity = datastoreService.get(key);
		
		assertEquals(true, featureEntity.getProperty(DatastoreStateRepository.ENABLED));
		assertEquals("someId", featureEntity.getProperty(DatastoreStateRepository.STRATEGY_ID));
		assertThat((List<String>) featureEntity.getProperty(DatastoreStateRepository.STRATEGY_PARAMS_NAMES), is(Arrays.asList("param")));           
		assertThat((List<String>) featureEntity.getProperty(DatastoreStateRepository.STRATEGY_PARAMS_VALUES), is(Arrays.asList("foo")));
    }    

    @Test
    public void testShouldReadStateWithoutStrategyAndParameters() {

        /*
         * GIVEN a database row containing a simple feature state
         */
        update("F1", false, null, null);

        /*
         * WHEN the repository reads the state
         */
        FeatureState state = repository.getFeatureState(TestFeature.F1);

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
    	Map<String, String> map = new HashMap<String, String>() {{
    		   put("param23", "foobar");
    		}};
    	
    	update("F1", true, "myStrategy", map);


        /*
         * WHEN the repository reads the state
         */
        FeatureState state = repository.getFeatureState(TestFeature.F1);

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
    public void testShouldUpdateExistingDatabaseEntry() throws EntityNotFoundException  {

        /*
         * GIVEN a database row containing a simple feature state
         */
		Map<String, String> map = new HashMap<String, String>() {{
 		   put("param23", "foobar");
 		}};
 		update("F1", true, "myStrategy", map);

        /*
         * AND the database entries are like expected
         */
        /*
         * THEN there should be a corresponding entry in the database
         */
		Key key = KeyFactory.createKey(repository.kind(), TestFeature.F1.name());
		Entity featureEntity = datastoreService.get(key);
		
		assertEquals(true, featureEntity.getProperty(DatastoreStateRepository.ENABLED));
		assertEquals("myStrategy", featureEntity.getProperty(DatastoreStateRepository.STRATEGY_ID));
		assertThat((List<String>) featureEntity.getProperty(DatastoreStateRepository.STRATEGY_PARAMS_NAMES), is(Arrays.asList("param23")));           
		assertThat((List<String>) featureEntity.getProperty(DatastoreStateRepository.STRATEGY_PARAMS_VALUES), is(Arrays.asList("foobar")));

        /*
         * WHEN the repository writes new state
         */
        FeatureState state = new FeatureState(TestFeature.F1)
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
		assertThat((List<String>) featureEntity.getProperty(DatastoreStateRepository.STRATEGY_PARAMS_NAMES), is(Arrays.asList("param")));           
		assertThat((List<String>) featureEntity.getProperty(DatastoreStateRepository.STRATEGY_PARAMS_VALUES), is(Arrays.asList("foo")));
    }
    
    
	private void update(String name, boolean enabled, String strategyId, Map<String, String> params) {
		Entity featureEntity = new Entity(repository.kind(), name);
		featureEntity.setUnindexedProperty(DatastoreStateRepository.ENABLED, enabled);
		featureEntity.setUnindexedProperty(DatastoreStateRepository.STRATEGY_ID, strategyId);
		
		if (params != null) {
			List<String> strategyParamsNames = new ArrayList<String>();
			List<String> strategyParamsValues = new ArrayList<String>(); 
			for (String paramName : params.keySet()) {
				strategyParamsNames.add(paramName);
				strategyParamsValues.add(params.get(paramName));
			}
			featureEntity.setUnindexedProperty(DatastoreStateRepository.STRATEGY_PARAMS_NAMES, strategyParamsNames);
			featureEntity.setUnindexedProperty(DatastoreStateRepository.STRATEGY_PARAMS_VALUES, strategyParamsValues);
		}		
		
		datastoreService.put(featureEntity);
	}


	private static enum TestFeature implements Feature {
        F1
    }
}
