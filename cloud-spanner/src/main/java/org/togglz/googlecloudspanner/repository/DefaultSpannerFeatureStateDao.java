package org.togglz.googlecloudspanner.repository;

import com.google.cloud.spanner.Key;
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.ReadOnlyTransaction;
import com.google.cloud.spanner.Struct;
import com.google.cloud.spanner.TransactionContext;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.util.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DefaultSpannerFeatureStateDao implements SpannerFeatureStateDao {
    static final String DEFAULT_FEATURE_STATE_TABLE_NAME = "FeatureToggle";
    static final String DEFAULT_STRATEGY_ID_COLUMN_NAME = "strategyId";
    static final String DEFAULT_ENABLED_COLUMN_NAME = "enabled";
    static final String DEFAULT_FEATURE_NAME_COLUMN_NAME = "featureName";
    static final String DEFAULT_STRATEGY_PARAMS_VALUES_COLUMN_NAME = "strategyParamsValues";
    static final String DEFAULT_STRATEGY_PARAMS_NAMES_COLUMN_NAME = "strategyParamsNames";

    private String featureStateTableName = DEFAULT_FEATURE_STATE_TABLE_NAME;
    private String featureNameColumnName = DEFAULT_FEATURE_NAME_COLUMN_NAME;
    private String enabledColumnName = DEFAULT_ENABLED_COLUMN_NAME;
    private String strategyIdColumnName = DEFAULT_STRATEGY_ID_COLUMN_NAME;
    private String strategyParamsNamesColumnName = DEFAULT_STRATEGY_PARAMS_NAMES_COLUMN_NAME;
    private String strategyParamsValuesColumnName = DEFAULT_STRATEGY_PARAMS_VALUES_COLUMN_NAME;

    public DefaultSpannerFeatureStateDao() {
    }

    public void setFeatureStateTableName(String featureStateTableName) {
        this.featureStateTableName = featureStateTableName;
    }

    public void setFeatureNameColumnName(String featureNameColumnName) {
        this.featureNameColumnName = featureNameColumnName;
    }

    public void setEnabledColumnName(String enabledColumnName) {
        this.enabledColumnName = enabledColumnName;
    }

    public void setStrategyIdColumnName(String strategyIdColumnName) {
        this.strategyIdColumnName = strategyIdColumnName;
    }

    public void setStrategyParamsNamesColumnName(String strategyParamsNamesColumnName) {
        this.strategyParamsNamesColumnName = strategyParamsNamesColumnName;
    }

    public void setStrategyParamsValuesColumnName(String strategyParamsValuesColumnName) {
        this.strategyParamsValuesColumnName = strategyParamsValuesColumnName;
    }

    public Optional<FeatureState> select(Feature feature, ReadOnlyTransaction tx) {
        Struct rowFound = readRow(feature, tx);
        return Optional.ofNullable(rowFound).map((row) -> fetchFeatureState(feature, row));
    }

    private Struct readRow(Feature feature, ReadOnlyTransaction tx) {
        return tx.readRow(featureStateTableName, Key.of(feature.name()), List.of(featureNameColumnName, enabledColumnName, strategyIdColumnName, strategyParamsNamesColumnName, strategyParamsValuesColumnName));
    }

    private FeatureState fetchFeatureState(Feature feature, Struct row) {
        boolean enabled = row.getBoolean(enabledColumnName);
        FeatureState state = new FeatureState(feature, enabled);

        String strategyId = getStrategyId(row);
        state.setStrategyId(strategyId);

        fetchStrategyParamsFromColumns(row, state);

        return state;
    }

    private void fetchStrategyParamsFromColumns(Struct row, FeatureState state) {
        List<String> names = valuesList(row, strategyParamsNamesColumnName);
        List<String> values = valuesList(row, strategyParamsValuesColumnName);
        Preconditions.checkState(names.size() == values.size());

        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            String value = values.get(i);
            state.setParameter(name, value);
        }
    }

    private List<String> valuesList(Struct row, String propertyName) {
        List<String> values = row.getStringList(propertyName);
        if (values == null) {
            return Collections.emptyList();
        }

        return values;
    }

    private String getStrategyId(Struct row) {
        final String strategyId = getStringOrNull(strategyIdColumnName, row);
        if (strategyId != null && !strategyId.isEmpty()) {
            return strategyId.trim();
        }

        return null;
    }

    private String getStringOrNull(String colName, Struct row) {
        return row.isNull(colName) ? null : row.getString(colName);
    }

    public void upsert(FeatureState featureState, TransactionContext tx) {
        Map<String, String> params = featureState.getParameterMap();
        if (params == null) {
            params = Collections.emptyMap();
        }
        final List<String> strategyParamsNames = new ArrayList<>(params.size());
        final List<String> strategyParamsValues = new ArrayList<>(params.size());
        for (final String paramName : params.keySet()) {
            strategyParamsNames.add(paramName);
            strategyParamsValues.add(params.get(paramName));
        }

        tx.buffer(List.of(Mutation.newInsertOrUpdateBuilder(featureStateTableName)
                .set(featureNameColumnName).to(featureState.getFeature().name())
                .set(enabledColumnName).to(featureState.isEnabled())
                .set(strategyIdColumnName).to(featureState.getStrategyId())
                .set(strategyParamsNamesColumnName).toStringArray(strategyParamsNames)
                .set(strategyParamsValuesColumnName).toStringArray(strategyParamsValues).build()));
    }

    public void delete(String featureName, TransactionContext tx) {
        tx.buffer(Mutation.delete(featureStateTableName, Key.of(featureName)));
    }
}
