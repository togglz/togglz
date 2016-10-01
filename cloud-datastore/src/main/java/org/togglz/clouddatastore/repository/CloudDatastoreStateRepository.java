package org.togglz.clouddatastore.repository;

import com.google.cloud.datastore.*;
import com.google.common.base.Strings;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by fabio on 30/09/16.
 */
public class CloudDatastoreStateRepository implements StateRepository {

    public static final String STRATEGY_PARAMS_VALUES = "strategyParamsValues";
    public static final String STRATEGY_PARAMS_NAMES = "strategyParamsNames";
    public static final String STRATEGY_ID = "strategyId";
    public static final String ENABLED = "enabled";
    private String kind = "FeatureToggle";

    private final Datastore datastore;
    private final KeyFactory keyFactory;


    @Inject
    public CloudDatastoreStateRepository(Datastore datastore) {
        this.datastore = datastore;
        keyFactory = this.datastore.newKeyFactory().kind(kind());
    }

    @Override
    public FeatureState getFeatureState(Feature feature) {
        final Key key = keyFactory.newKey(feature.name());
        final Entity featureEntity = this.datastore.get(key);
        return createFeatureState(feature, featureEntity);
    }

    /**
     * @param feature
     * @param featureEntity
     * @return
     */
    private FeatureState createFeatureState(final Feature feature, final Entity featureEntity) {
        if (featureEntity == null) {
            return null;
        }

        final Boolean enabled = featureEntity.getBoolean(ENABLED);
        final FeatureState state = new FeatureState(feature, enabled);

        final String strategyId = featureEntity.getString(STRATEGY_ID);
        if (!Strings.isNullOrEmpty(strategyId)) {
            state.setStrategyId(strategyId.trim());
        }

        List<Value<String>> strategyParamsNames = featureEntity.getList(STRATEGY_PARAMS_NAMES);
        List<Value<String>> strategyParamsValues = featureEntity.getList(STRATEGY_PARAMS_VALUES);
        if (strategyParamsNames != null && strategyParamsValues != null && !strategyParamsNames.isEmpty()
                && !strategyParamsValues.isEmpty() && strategyParamsNames.size() == strategyParamsValues.size()) {
            for (int i = 0; i < strategyParamsNames.size(); i++) {
                state.setParameter(strategyParamsNames.get(i).get(), strategyParamsValues.get(i).get());
            }
        }
        return state;
    }

    @Override
    public void setFeatureState(FeatureState featureState) {
        final Key key = keyFactory.newKey(featureState.getFeature().name());
        final Entity.Builder builder = Entity.builder(key)
                .set(ENABLED, BooleanValue.builder(featureState.isEnabled()).excludeFromIndexes(true).build())
                .set(STRATEGY_ID, StringValue.builder(featureState.getStrategyId()).excludeFromIndexes(true).build());

        final Map<String, String> params = featureState.getParameterMap();
        if (params != null && !params.isEmpty()) {
            final List<Value<String>> strategyParamsNames = new ArrayList<>(params.size());
            final List<Value<String>> strategyParamsValues = new ArrayList<>(params.size());
            for (final String paramName : params.keySet()) {
                strategyParamsNames.add(StringValue.builder(paramName).excludeFromIndexes(true).build());
                strategyParamsValues.add(StringValue.builder(params.get(paramName)).excludeFromIndexes(true).build());
            }
            builder.set(STRATEGY_PARAMS_NAMES, strategyParamsNames);
            builder.set(STRATEGY_PARAMS_VALUES, strategyParamsValues);
        }

        this.datastore.put(builder.build());
    }


    protected String kind() {
        return this.kind;
    }


}
