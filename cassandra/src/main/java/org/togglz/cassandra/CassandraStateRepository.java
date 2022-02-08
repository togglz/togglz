package org.togglz.cassandra;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.util.DefaultMapSerializer;
import org.togglz.core.repository.util.MapSerializer;
import com.netflix.astyanax.ColumnListMutation;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.MutationBatch;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.ddl.KeyspaceDefinition;
import com.netflix.astyanax.model.Column;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.ColumnList;
import com.netflix.astyanax.model.ConsistencyLevel;
import com.netflix.astyanax.serializers.StringSerializer;

/**
 * <p>
 * This repository implementation can be used to store the feature state in Cassandra using Astyanax client.
 * </p>
 *
 * <p>
 * {@link org.togglz.cassandra.CassandraStateRepository} stores the feature state in a column family called <code>Togglz</code> by default.
 * You can choose the name of this column family using a builder provided by this class.
 *
 * If the repository doesn't find the column family, it will automatically create it.
 * </p>
 *
 * <p>
 * The column family has the following format:
 * </p>
 *
 * <pre>
 * create column family Togglz
 *    with comparator = 'UTF8Type'
 *    and default_validation_class = UTF8Type
 *    and key_validation_class = UTF8Type;
 * </pre>
 *
 * <p>
 * The class provides a builder which can be used to configure the repository:
 * </p>
 *
 * <pre>
 * StateRepository repository = CassandraStateRepository.newBuilder(keyspace)
 *     .columnFamily(&quot;Togglz&quot;)
 *     .autoCreateColumnFamily(false)
 *     .mapSerializer(DefaultMapSerializer.singleline())
 *     .build();
 * </pre>
 *
 * <p>
 * You need to provide a {@link Keyspace} to use {@link org.togglz.cassandra.CassandraStateRepository}.
 * It can be created using {@link KeyspaceBuilder} or any other way.
 * </p>
 *
 * @author artur@callfire.com
 */
public class CassandraStateRepository implements StateRepository {

    private static final String ENABLED_COLUMN = "enabled";
    private static final String STRATEGY_ID_COLUMN = "strategy_id";
    private static final String STRATEGY_PARAMS_COLUMN = "strategy_params";

    private static final String DEFAULT_COLUMN_FAMILY_NAME = "Togglz";

    private final Keyspace keyspace;
    private final ColumnFamily<String, String> columnFamily;
    private final MapSerializer mapSerializer;

    public CassandraStateRepository(final Keyspace keyspace) {
        this(new Builder(keyspace));
    }

    private CassandraStateRepository(final Builder builder) {
        this.keyspace = builder.keyspace;
        this.columnFamily = builder.columnFamily;
        this.mapSerializer = builder.mapSerializer;

        if (builder.autoCreateColumnFamily) {
            initColumnFamily();
        }
    }

    private void initColumnFamily() {
        try {
            final KeyspaceDefinition keyspaceDefinition = keyspace.describeKeyspace();
            if (keyspaceDefinition.getColumnFamily(columnFamily.getName()) == null) {

                final Map<String, Object> parameters = new HashMap<String, Object>();
                parameters.put("default_validation_class", "UTF8Type");
                parameters.put("key_validation_class", "UTF8Type");
                parameters.put("comparator_type", "UTF8Type");

                keyspace.createColumnFamily(columnFamily, parameters);
            }
        }
        catch (final ConnectionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FeatureState getFeatureState(Feature feature) {
        try {
            final ColumnList<String> state = keyspace
                    .prepareQuery(columnFamily)
                    .getRow(feature.name())
                    .execute()
                    .getResult();

            return state.isEmpty() ? null : toFeatureState(feature, state);
        }
        catch (ConnectionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setFeatureState(final FeatureState featureState) {
        final MutationBatch mutationBatch = keyspace.prepareMutationBatch().setConsistencyLevel(ConsistencyLevel.CL_QUORUM);

        final ColumnListMutation<String> mutation = mutationBatch
            .withRow(columnFamily, featureState.getFeature().name())
            .putColumn(ENABLED_COLUMN, featureState.isEnabled());

        putOrDelete(mutation, STRATEGY_ID_COLUMN, featureState.getStrategyId());
        putOrDelete(mutation, STRATEGY_PARAMS_COLUMN, mapSerializer.serialize(featureState.getParameterMap()));

        try {
            mutationBatch.execute();
        }
        catch (final ConnectionException e) {
            throw new RuntimeException(e);
        }
    }

    private void putOrDelete(final ColumnListMutation<String> mutation, final String column, final String value) {
        if (value == null || value.trim().isEmpty()) {
            mutation.deleteColumn(column);
        } else {
            mutation.putColumn(column, value);
        }
    }

    private FeatureState toFeatureState(Feature feature, ColumnList<String> state) {
        final Column<String> enabled = state.getColumnByName(ENABLED_COLUMN);
        final Column<String> strategyId = state.getColumnByName(STRATEGY_ID_COLUMN);
        final Column<String> strategyValues = state.getColumnByName(STRATEGY_PARAMS_COLUMN);

        final FeatureState featureState = new FeatureState(feature);
        featureState.setEnabled(enabled != null && enabled.getBooleanValue());
        featureState.setStrategyId(strategyId != null ? strategyId.getStringValue() : null);

        if (strategyValues != null) {
            final Map<String, String> params = mapSerializer.deserialize(strategyValues.getStringValue());
            for (final Entry<String, String> entry : params.entrySet()) {
                featureState.setParameter(entry.getKey(), entry.getValue());
            }
        }

        return featureState;
    }

    /**
     * Creates a new builder for creating a {@link org.togglz.cassandra.CassandraStateRepository}.
     *
     * @param keyspace the {@link Keyspace} Togglz should use to query cassandra.
     * Can be created using the {@link KeyspaceBuilder}.
     */
    public static Builder newBuilder(final Keyspace keyspace) {
        return new Builder(keyspace);
    }

    /**
     * Builder for a {@link org.togglz.cassandra.CassandraStateRepository}
     **/
    public static class Builder {
        private final Keyspace keyspace;

        private boolean autoCreateColumnFamily = true;
        private MapSerializer mapSerializer = DefaultMapSerializer.multiline();
        private ColumnFamily<String, String> columnFamily = new ColumnFamily<String, String>(DEFAULT_COLUMN_FAMILY_NAME,
                StringSerializer.get(), StringSerializer.get());

        private Builder(final Keyspace keyspace) {
            this.keyspace = keyspace;
        }

        /**
         * If set to <code>true</code>, the column family will be automatically created if it is missing. The default is
         * <code>true</code>.
         *
         * @param autoCreate <code>true</code> if the table should be created automatically
         */
        public Builder autoCreateColumnFamily(final boolean autoCreate) {
            this.autoCreateColumnFamily = autoCreate;
            return this;
        }

        /**
         * The {@link MapSerializer} for storing parameters. By default the repository will use
         * {@link DefaultMapSerializer#multiline()}.
         *
         * @param serializer The serializer to use
         */
        public Builder mapSerializer(final MapSerializer serializer) {
            this.mapSerializer = serializer;
            return this;
        }

        /**
         * Sets the column family name to use for the Togglz feature state. The default name is <code>Togglz</code>.
         *
         * @param columnFamilyName column family name to use
         */
        public Builder columnFamily(final String columnFamilyName) {
            this.columnFamily =
                    new ColumnFamily<String, String>(columnFamilyName, StringSerializer.get(), StringSerializer.get());
            return this;
        }

        /**
         * Creates a {@link org.togglz.cassandra.CassandraStateRepository} from the current configuration
         */
        public CassandraStateRepository build() {
            return new CassandraStateRepository(this);
        }
    }
}
