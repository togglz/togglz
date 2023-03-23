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
import com.google.appengine.api.datastore.Transaction;

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

    private final DatastoreService datastoreService;
    private String kind = "FeatureToggle";

    public DatastoreStateRepository(final DatastoreService datastoreService) {
        super();
        this.datastoreService = datastoreService;
    }

    public DatastoreStateRepository(final String kind, final DatastoreService datastoreService) {
        this(datastoreService);
        this.kind = kind;
    }

    @Override
    public FeatureState getFeatureState(final Feature feature) {
        try {
            final Key key = KeyFactory.createKey(kind(), feature.name());
            return createFeatureState(feature, getInsideTransaction(key));
        } catch (final EntityNotFoundException ignored) {
            return null;
        }
    }

    /**
     * @param feature
     * @param featureEntity
     * @return
     */
    @SuppressWarnings("unchecked")
    private FeatureState createFeatureState(final Feature feature, final Entity featureEntity) {
        final Boolean enabled = (Boolean) featureEntity.getProperty(ENABLED);
        final FeatureState state = new FeatureState(feature, enabled);

        final String strategyId = (String) featureEntity.getProperty(STRATEGY_ID);
        if (strategyId != null && !strategyId.isEmpty()) {
            state.setStrategyId(strategyId.trim());
        }

        final List<String> strategyParamsNames = (List<String>) featureEntity.getProperty(STRATEGY_PARAMS_NAMES);
        final List<String> strategyParamsValues = (List<String>) featureEntity.getProperty(STRATEGY_PARAMS_VALUES);
        if (strategyParamsNames != null && strategyParamsValues != null && !strategyParamsNames.isEmpty()
            && !strategyParamsValues.isEmpty() && strategyParamsNames.size() == strategyParamsValues.size()) {
            for (int i = 0; i < strategyParamsNames.size(); i++) {
                state.setParameter(strategyParamsNames.get(i), strategyParamsValues.get(i));
            }
        }
        return state;
    }

    @Override
    public void setFeatureState(final FeatureState featureState) {
        final Entity featureEntity = new Entity(kind(), featureState.getFeature().name());
        featureEntity.setUnindexedProperty(ENABLED, featureState.isEnabled());
        featureEntity.setUnindexedProperty(STRATEGY_ID, featureState.getStrategyId());

        final Map<String, String> params = featureState.getParameterMap();
        if (params != null && !params.isEmpty()) {
            final List<String> strategyParamsNames = new ArrayList<String>(params.size());
            final List<String> strategyParamsValues = new ArrayList<String>(params.size());
            for (final String paramName : params.keySet()) {
                strategyParamsNames.add(paramName);
                strategyParamsValues.add(params.get(paramName));
            }
            featureEntity.setUnindexedProperty(STRATEGY_PARAMS_NAMES, strategyParamsNames);
            featureEntity.setUnindexedProperty(STRATEGY_PARAMS_VALUES, strategyParamsValues);
        }

        putInsideTransaction(featureEntity);

    }

    /**
     * @param featureEntity
     */
    protected void putInsideTransaction(final Entity featureEntity) {
        if (this.datastoreService.getCurrentTransaction(null) == null) {
            this.datastoreService.put(featureEntity);
        } else {
            final Transaction txn = this.datastoreService.beginTransaction();
            try {
                this.datastoreService.put(txn, featureEntity);
                txn.commit();
            } finally {
                if (txn.isActive()) {
                    txn.rollback();
                }
            }
        }
    }

    /**
     * @param key
     * @return
     * @throws EntityNotFoundException
     */
    protected Entity getInsideTransaction(final Key key) throws EntityNotFoundException {
        Entity featureEntity;
        if (this.datastoreService.getCurrentTransaction(null) == null) {
            featureEntity = this.datastoreService.get(key);
        } else {
            final Transaction txn = this.datastoreService.beginTransaction();
            try {
                featureEntity = this.datastoreService.get(txn, key);
                txn.commit();
            } finally {
                if (txn.isActive()) {
                    txn.rollback();
                }
            }
        }
        return featureEntity;
    }

    protected String kind() {
        return this.kind;
    }

}