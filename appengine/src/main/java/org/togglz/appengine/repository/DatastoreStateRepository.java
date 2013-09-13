package org.togglz.appengine.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.common.base.Strings;

/**
 * <p>
 * This repository implementation can be used to store the feature state in Appengine's Datastore.
 * </p>
 * 
 * <p>
 * {@link DatastoreStateRepository} stores the feature state in the FeatureToggle kind.
 * </p>
 * 
 * @author Fábio Franco Uechi
 */
public class DatastoreStateRepository implements StateRepository {

	public static final String STRATEGY_PARAMS_VALUES = "strategyParamsValues";
	public static final String STRATEGY_PARAMS_NAMES = "strategyParamsNames";
	public static final String STRATEGY_ID = "strategyId";
	public static final String ENABLED = "enabled";
	
	private DatastoreService datastoreService;

	public DatastoreStateRepository(final DatastoreService datastoreService) {
		super();
		this.datastoreService = datastoreService;
	}

	@SuppressWarnings("unchecked")
	@Override
	public FeatureState getFeatureState(Feature feature) {
		FeatureState state = null;
		try {
			
			Key key = KeyFactory.createKey(getKind(), feature.name());
			Entity featureEntity = datastoreService.get(key);
			
			Boolean enabled = (Boolean) featureEntity.getProperty(ENABLED);
			state = new FeatureState(feature, enabled);
			
			String strategyId = (String) featureEntity.getProperty(STRATEGY_ID);
			if (!Strings.isNullOrEmpty(strategyId)) {
				state.setStrategyId(strategyId.trim());
			}
			
			List<String> strategyParamsNames = (List<String>) featureEntity.getProperty(STRATEGY_PARAMS_NAMES);
			List<String> strategyParamsValues = (List<String>) featureEntity.getProperty(STRATEGY_PARAMS_VALUES);
			if (strategyParamsNames != null
					&& strategyParamsValues != null
					&& !strategyParamsNames.isEmpty()
					&& !strategyParamsValues.isEmpty()
					&& strategyParamsNames.size() == strategyParamsValues
							.size()) {
				for (int i=0; i < strategyParamsNames.size(); i++  ) {
					state.setParameter(strategyParamsNames.get(i), strategyParamsValues.get(i));
				}
			}
			
		} catch (EntityNotFoundException e) {
		}
		
		return state;
	}

	@Override
	public void setFeatureState(FeatureState featureState) {
		Entity featureEntity = new Entity(getKind(), featureState.getFeature().name());
		featureEntity.setUnindexedProperty(ENABLED, featureState.isEnabled());
		featureEntity.setUnindexedProperty(STRATEGY_ID, featureState.getStrategyId());
		
		Map<String, String> params = featureState.getParameterMap();
		if (params != null && !params.isEmpty()) {
			List<String> strategyParamsNames = new ArrayList<String>(params.size());
			List<String> strategyParamsValues = new ArrayList<String>(params.size()); 
			for (String paramName : params.keySet()) {
				strategyParamsNames.add(paramName);
				strategyParamsValues.add(params.get(paramName));
			}
			featureEntity.setUnindexedProperty(STRATEGY_PARAMS_NAMES, strategyParamsNames);
			featureEntity.setUnindexedProperty(STRATEGY_PARAMS_VALUES, strategyParamsValues);
		}
		datastoreService.put(featureEntity);
	}

	/**
	 * @return
	 */
	protected String getKind() {
		return "FeatureToggle";
	}

}
