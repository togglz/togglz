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

        if (featureEntity.contains(STRATEGY_ID)) {
            final String strategyId = featureEntity.getString(STRATEGY_ID);
            if (!Strings.isNullOrEmpty(strategyId)) {
                state.setStrategyId(strategyId.trim());
            }
        }

        if (featureEntity.contains(STRATEGY_PARAMS_NAMES) && featureEntity.contains(STRATEGY_PARAMS_VALUES)) {
            List<Value<String>> strategyParamsNames = featureEntity.getList(STRATEGY_PARAMS_NAMES);
            List<Value<String>> strategyParamsValues = featureEntity.getList(STRATEGY_PARAMS_VALUES);
            if (strategyParamsNames != null && strategyParamsValues != null && !strategyParamsNames.isEmpty()
                    && !strategyParamsValues.isEmpty() && strategyParamsNames.size() == strategyParamsValues.size()) {
                for (int i = 0; i < strategyParamsNames.size(); i++) {
                    state.setParameter(strategyParamsNames.get(i).get(), strategyParamsValues.get(i).get());
                }
            }
        }
        return state;
    }

    @Override
    public void setFeatureState(FeatureState featureState) {
        final Key key = keyFactory.newKey(featureState.getFeature().name());
        final Entity.Builder builder = Entity.newBuilder(key)
                .set(ENABLED, BooleanValue.newBuilder(featureState.isEnabled()).setExcludeFromIndexes(true).build());

        if (featureState.getStrategyId() != null) {
            builder.set(STRATEGY_ID, StringValue.newBuilder(featureState.getStrategyId()).setExcludeFromIndexes(true).build());
        }

        final Map<String, String> params = featureState.getParameterMap();
        if (params != null && !params.isEmpty()) {
            final List<Value<String>> strategyParamsNames = new ArrayList<>(params.size());
            final List<Value<String>> strategyParamsValues = new ArrayList<>(params.size());
            for (final String paramName : params.keySet()) {
                strategyParamsNames.add(StringValue.newBuilder(paramName).setExcludeFromIndexes(true).build());
                strategyParamsValues.add(StringValue.newBuilder(params.get(paramName)).setExcludeFromIndexes(true).build());
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
