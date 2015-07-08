package org.togglz.cassandra;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
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
    private final boolean autoCreateColumnFamily;
    private final MapSerializer mapSerializer;

    public CassandraStateRepository(Keyspace keyspace) {
        this(new Builder(keyspace));
    }

    private CassandraStateRepository(Builder builder) {
        this.keyspace = builder.keyspace;
        this.columnFamily = builder.columnFamily;
        this.autoCreateColumnFamily = builder.autoCreateColumnFamily;
        this.mapSerializer = builder.mapSerializer;

        if (autoCreateColumnFamily) {
            initColumnFamily();
        }
    }

    private void initColumnFamily() {
        try {
            KeyspaceDefinition keyspaceDefinition = keyspace.describeKeyspace();
            if (keyspaceDefinition.getColumnFamily(columnFamily.getName()) == null) {

                Map<String, Object> parameters = new HashMap<String, Object>();
                parameters.put("default_validation_class", "UTF8Type");
                parameters.put("key_validation_class", "UTF8Type");
                parameters.put("comparator_type", "UTF8Type");

                keyspace.createColumnFamily(columnFamily, parameters);
            }
        }
        catch (ConnectionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FeatureState getFeatureState(Feature feature) {
        try {
            ColumnList<String> state = keyspace
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
    public void setFeatureState(FeatureState featureState) {
        MutationBatch mutationBatch = keyspace.prepareMutationBatch().setConsistencyLevel(ConsistencyLevel.CL_QUORUM);

        ColumnListMutation<String> mutation = mutationBatch
            .withRow(columnFamily, featureState.getFeature().name())
            .putColumn(ENABLED_COLUMN, featureState.isEnabled());

        putOrDelete(mutation, STRATEGY_ID_COLUMN, featureState.getStrategyId());
        putOrDelete(mutation, STRATEGY_PARAMS_COLUMN, mapSerializer.serialize(featureState.getParameterMap()));

        try {
            mutationBatch.execute();
        }
        catch (ConnectionException e) {
            throw new RuntimeException(e);
        }
    }

	private void putOrDelete(ColumnListMutation mutation, String column, String value) {
		if (StringUtils.isBlank(value)) {
			mutation.deleteColumn(column);
		}
		else {
			mutation.putColumn(column, value);
		}
	}

    private FeatureState toFeatureState(Feature feature, ColumnList<String> state) {
        Column<String> enabled = state.getColumnByName(ENABLED_COLUMN);
        Column<String> strategyId = state.getColumnByName(STRATEGY_ID_COLUMN);
        Column<String> strategyValues = state.getColumnByName(STRATEGY_PARAMS_COLUMN);

        FeatureState featureState = new FeatureState(feature);
        featureState.setEnabled(enabled != null ? enabled.getBooleanValue() : false);
        featureState.setStrategyId(strategyId != null ? strategyId.getStringValue() : null);

        if (strategyValues != null) {
            Map<String, String> params = mapSerializer.deserialize(strategyValues.getStringValue());
            for (Entry<String, String> entry : params.entrySet()) {
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
    public static Builder newBuilder(Keyspace keyspace) {
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

        private Builder(Keyspace keyspace) {
            this.keyspace = keyspace;
        }

        /**
         * If set to <code>true</code>, the column family will be automatically created if it is missing. The default is
         * <code>true</code>.
         *
         * @param autoCreate <code>true</code> if the table should be created automatically
         */
        public Builder autoCreateColumnFamily(boolean autoCreate) {
            this.autoCreateColumnFamily = autoCreate;
            return this;
        }

        /**
         * The {@link MapSerializer} for storing parameters. By default the repository will use
         * {@link DefaultMapSerializer#multiline()}.
         *
         * @param serializer The serializer to use
         */
        public Builder mapSerializer(MapSerializer serializer) {
            this.mapSerializer = serializer;
            return this;
        }

        /**
         * Sets the column family name to use for the Togglz feature state. The default name is <code>Togglz</code>.
         *
         * @param columnFamilyName column family name to use
         */
        public Builder columnFamily(String columnFamilyName) {
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
