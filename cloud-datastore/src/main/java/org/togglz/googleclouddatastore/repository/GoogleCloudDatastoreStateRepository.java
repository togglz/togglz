package org.togglz.googleclouddatastore.repository;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Value;
import javax.inject.Inject;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.util.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * This repository implementation can be used to store the feature state
 * in <a href="https://cloud.google.com/datastore/docs/">Google Cloud Datastore</>
 * </p>
 *
 * <p>
 * {@link GoogleCloudDatastoreStateRepository} stores the feature state in the FeatureToggle kind.
 * </p>
 *
 * @author Fábio Franco Uechi
 */
public class GoogleCloudDatastoreStateRepository implements StateRepository {

    static final String STRATEGY_PARAMS_VALUES = "strategyParamsValues";
    static final String STRATEGY_PARAMS_NAMES = "strategyParamsNames";
    static final String STRATEGY_ID = "strategyId";
    static final String ENABLED = "enabled";
    static final String KIND_DEFAULT = "FeatureToggle";

    private final Datastore datastore;
    private final KeyFactory keyFactory;

    @Inject
    public GoogleCloudDatastoreStateRepository(Datastore datastore) {
        this(datastore, KIND_DEFAULT);
    }

    @Inject
    public GoogleCloudDatastoreStateRepository(final Datastore datastore, final String kind) {
        this.datastore = datastore;
        this.keyFactory = this.datastore.newKeyFactory().setKind(kind);
    }

    @Override
    public FeatureState getFeatureState(Feature feature) {
        final Key key = createKey(feature);
        final Entity featureEntity = this.datastore.get(key);
        return createFeatureState(feature, featureEntity);
    }

    private Key createKey(Feature feature) {
        return keyFactory.newKey(feature.name());
    }

    private FeatureState createFeatureState(final Feature feature, final Entity featureEntity) {
        if (featureEntity == null) {
            return null;
        }

        final Boolean enabled = featureEntity.getBoolean(ENABLED);
        final FeatureState state = new FeatureState(feature, enabled);

        state.setStrategyId(getStrategyId(featureEntity));

        List<Value<String>> names = valuesList(featureEntity, STRATEGY_PARAMS_NAMES);
        List<Value<String>> values = valuesList(featureEntity, STRATEGY_PARAMS_VALUES);
        Preconditions.checkState(names.size() == values.size());
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i).get();
            String value = values.get(i).get();
            state.setParameter(name, value);
        }

        return state;
    }

    private List<Value<String>> valuesList(Entity entity, String propertyName) {
        return entity.contains(propertyName) ?
                entity.<Value<String>>getList(propertyName) : Collections.<Value<String>>emptyList();
    }

    private String getStrategyId(Entity featureEntity) {
        if (featureEntity.contains(STRATEGY_ID)) {
            final String strategyId = featureEntity.getString(STRATEGY_ID);
            if (strategyId != null && !strategyId.isEmpty()) {
                return strategyId.trim();
            }
        }
        return null;
    }

    @Override
    public void setFeatureState(FeatureState featureState) {
        final Key key = createKey(featureState.getFeature());
        final Entity.Builder builder = Entity.newBuilder(key)
                .set(ENABLED, NonIndexed.valueOf(featureState.isEnabled()));

        if (featureState.getStrategyId() != null) {
            builder.set(STRATEGY_ID, NonIndexed.valueOf(featureState.getStrategyId()));
        }

        final Map<String, String> params = featureState.getParameterMap();
        if (params != null && !params.isEmpty()) {
            final List<Value<String>> strategyParamsNames = new ArrayList<>(params.size());
            final List<Value<String>> strategyParamsValues = new ArrayList<>(params.size());
            for (final String paramName : params.keySet()) {
                strategyParamsNames.add(NonIndexed.valueOf(paramName));
                strategyParamsValues.add(NonIndexed.valueOf(params.get(paramName)));
            }
            builder.set(STRATEGY_PARAMS_NAMES, strategyParamsNames);
            builder.set(STRATEGY_PARAMS_VALUES, strategyParamsValues);
        }

        this.datastore.put(builder.build());
    }

}
