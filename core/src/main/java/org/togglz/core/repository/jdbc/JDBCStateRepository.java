package org.togglz.core.repository.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.util.DefaultMapSerializer;
import org.togglz.core.repository.util.MapSerializer;
import org.togglz.core.util.DbUtils;
import org.togglz.core.util.Strings;

/**
 * <p>
 * This repository implementation can be used to store the feature state in SQL database using the standard JDBC API.
 * </p>
 *
 * <p>
 * {@link JDBCStateRepository} stores the feature state in a single database table. You can choose the name of this table using
 * a constructor argument. If the repository doesn't find the required table in the database, it will automatically create it.
 * </p>
 *
 * <p>
 * The database table has the following format:
 * </p>
 *
 * <pre>
 * CREATE TABLE &lt;table&gt; (
 *   FEATURE_NAME VARCHAR(100) PRIMARY KEY,
 *   FEATURE_ENABLED INTEGER,
 *   STRATEGY_ID VARCHAR(200),
 *   STRATEGY_PARAMS VARCHAR(2000)
 * )
 * </pre>
 *
 * <p>
 * The class provides a builder which can be used to configure the repository:
 * </p>
 *
 * <pre>
 * StateRepository repository = JDBCStateRepository.newBuilder(dataSource)
 *     .tableName(&quot;features&quot;)
 *     .createTable(false)
 *     .serializer(DefaultMapSerializer.singleline())
 *     .noCommit(true)
 *     .build();
 * </pre>
 *
 * <p>
 * Please note that the structure of the database table changed with version 2.0.0 because of the new extensible activation
 * strategy mechanism. The table structure will be automatically migrated to the new format.
 * </p>
 *
 * @author Christian Kaltepoth
 *
 */
public class JDBCStateRepository implements StateRepository {

    protected final DataSource dataSource;

    protected final String tableName;

    protected final MapSerializer serializer;

    protected final boolean noCommit;

    /**
     * Constructor of {@link JDBCStateRepository}. A database table called <code>TOGGLZ</code> will be created automatically for
     * you.
     *
     * @param dataSource The JDBC {@link DataSource} to obtain connections from
     * @see #JDBCStateRepository(DataSource, String)
     */
    public JDBCStateRepository(DataSource dataSource) {
        this(new Builder(dataSource));
    }

    /**
     * Constructor of {@link JDBCStateRepository}. The database table will be created automatically for you.
     *
     * @param dataSource The JDBC {@link DataSource} to obtain connections from
     * @param tableName The name of the database table to use
     */
    public JDBCStateRepository(DataSource dataSource, String tableName) {
        this(new Builder(dataSource).tableName(tableName));
    }

    /**
     * Constructor of {@link JDBCStateRepository}.
     *
     * @param dataSource The JDBC {@link DataSource} to obtain connections from
     * @param tableName The name of the database table to use
     * @param createTable If set to <code>true</code>, the table will be automatically created if it is missing
     * @deprecated use {@link JDBCStateRepository#newBuilder(DataSource)} to create a builder that can be used to configure all
     *             aspects of the repository in a fluent way
     */
    @Deprecated
    public JDBCStateRepository(DataSource dataSource, String tableName, boolean createTable) {
        this(new Builder(dataSource).tableName(tableName).createTable(createTable));
    }

    /**
     * Constructor of {@link JDBCStateRepository}.
     *
     * @param dataSource The JDBC {@link DataSource} to obtain connections from
     * @param tableName The name of the database table to use
     * @param createTable If set to <code>true</code>, the table will be automatically created if it is missing
     * @param serializer The {@link MapSerializer} for storing parameters
     * @deprecated use {@link JDBCStateRepository#newBuilder(DataSource)} to create a builder that can be used to configure all
     *             aspects of the repository in a fluent way
     */
    @Deprecated
    public JDBCStateRepository(DataSource dataSource, String tableName, boolean createTable, MapSerializer serializer) {
        this(new Builder(dataSource).tableName(tableName).createTable(createTable).serializer(serializer));
    }

    /**
     * Constructor of {@link JDBCStateRepository}.
     *
     * @param dataSource The JDBC {@link DataSource} to obtain connections from
     * @param tableName The name of the database table to use
     * @param createTable If set to <code>true</code>, the table will be automatically created if it is missing
     * @param serializer The {@link MapSerializer} for storing parameters
     * @deprecated use {@link JDBCStateRepository#newBuilder(DataSource)} to create a builder that can be used to configure all
     *             aspects of the repository in a fluent way
     */
    public JDBCStateRepository(DataSource dataSource, String tableName, boolean createTable, MapSerializer serializer,
        boolean noCommit) {
        this(new Builder(dataSource).tableName(tableName).createTable(createTable).serializer(serializer).noCommit(noCommit));
    }

    /**
     * Private constructor initializing the class from a builder
     */
    private JDBCStateRepository(Builder builder) {
        this.dataSource = builder.dataSource;
        this.tableName = builder.tableName;
        this.serializer = builder.serializer;
        this.noCommit = builder.noCommit;
        if (builder.createTable) {
            migrateSchema();
        }
    }

    /**
     * Method for creating/migrating the database schema
     */
    protected void migrateSchema() {

        try {

            Connection connection = dataSource.getConnection();
            try {

                beforeSchemaMigration(connection);

                SchemaUpdater updater = new SchemaUpdater(connection, tableName, serializer);
                if (!updater.doesTableExist()) {
                    updater.migrateToVersion1();
                }
                if (updater.isSchemaVersion1()) {
                    updater.migrateToVersion2();
                }

                afterSchemaMigration(connection);


            } finally {
                DbUtils.closeQuietly(connection);
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to migrate the database schema", e);
        }

    }

    /**
     * Method called <strong>before</strong> the database schema migration is performed.
     */
    @SuppressWarnings("unused")
    protected void beforeSchemaMigration(Connection connection) {
        // overwrite me
    }

    /**
     * Method called <strong>after</strong> the database schema migration has been performed.
     */
    @SuppressWarnings("unused")
    protected void afterSchemaMigration(Connection connection) {
        // overwrite me
    }

    @Override
    public FeatureState getFeatureState(Feature feature) {

        try {

            Connection connection = dataSource.getConnection();
            try {

                String sql = "SELECT FEATURE_ENABLED, STRATEGY_ID, STRATEGY_PARAMS FROM %TABLE% WHERE FEATURE_NAME = ?";
                PreparedStatement statement = connection.prepareStatement(insertTableName(sql));
                try {

                    statement.setString(1, feature.name());

                    ResultSet resultSet = statement.executeQuery();
                    try {

                        if (resultSet.next()) {

                            boolean enabled = resultSet.getInt(Columns.FEATURE_ENABLED) > 0;
                            FeatureState state = new FeatureState(feature, enabled);

                            String strategyId = resultSet.getString(Columns.STRATEGY_ID);
                            if (Strings.isNotBlank(strategyId)) {
                                state.setStrategyId(strategyId.trim());
                            }

                            String paramData = resultSet.getString(Columns.STRATEGY_PARAMS);
                            if (Strings.isNotBlank(paramData)) {
                                Map<String, String> params = serializer.deserialize(paramData);
                                for (Entry<String, String> param : params.entrySet()) {
                                    state.setParameter(param.getKey(), param.getValue());
                                }
                            }

                            return state;

                        }

                    } finally {
                        DbUtils.closeQuietly(resultSet);
                    }

                } finally {
                    DbUtils.closeQuietly(statement);
                }

            } finally {
                DbUtils.closeQuietly(connection);
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to fetch the feature's state from the database", e);
        }

        return null;
    }

    @Override
    public void setFeatureState(FeatureState featureState) {

        try {

            Connection connection = dataSource.getConnection();
            try {

                int updatedRows = 0;

                /*
                 * First try to update an existing row
                 */
                String updateSql = "UPDATE %TABLE% SET FEATURE_ENABLED = ?, STRATEGY_ID = ?, STRATEGY_PARAMS = ? WHERE FEATURE_NAME = ?";
                PreparedStatement updateStatement = connection.prepareStatement(insertTableName(updateSql));
                try {

                    String paramData = serializer.serialize(featureState.getParameterMap());

                    updateStatement.setInt(1, featureState.isEnabled() ? 1 : 0);
                    updateStatement.setString(2, Strings.trimToNull(featureState.getStrategyId()));
                    updateStatement.setString(3, Strings.trimToNull(paramData));
                    updateStatement.setString(4, featureState.getFeature().name());

                    updatedRows = updateStatement.executeUpdate();

                } finally {
                    DbUtils.closeQuietly(updateStatement);
                }

                /*
                 * If our update didn't modify any data we have to insert a new row
                 */
                if (updatedRows == 0) {

                    String insertSql = "INSERT INTO %TABLE% (FEATURE_NAME, FEATURE_ENABLED, STRATEGY_ID, STRATEGY_PARAMS) VALUES (?,?,?,?)";
                    PreparedStatement insertStatement = connection.prepareStatement(insertTableName(insertSql));
                    try {

                        String paramsAsString = serializer.serialize(featureState.getParameterMap());

                        insertStatement.setString(1, featureState.getFeature().name());
                        insertStatement.setInt(2, featureState.isEnabled() ? 1 : 0);
                        insertStatement.setString(3, Strings.trimToNull(featureState.getStrategyId()));
                        insertStatement.setString(4, Strings.trimToNull(paramsAsString));

                        insertStatement.executeUpdate();

                    } finally {
                        DbUtils.closeQuietly(insertStatement);
                    }

                }

                if (!connection.getAutoCommit() && !noCommit) {
                    connection.commit();
                }

            } finally {
                DbUtils.closeQuietly(connection);
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to set the feature's state in the database", e);
        }

    }

    private String insertTableName(String s) {
        return s.replace("%TABLE%", tableName);
    }

    /**
     * Creates a new builder for creating a {@link JDBCStateRepository}.
     *
     * @param dataSource the {@link DataSource} Togglz should use to obtain JDBC connections
     */
    public static Builder newBuilder(DataSource dataSource) {
        return new Builder(dataSource);
    }

    /**
     * Builder for a {@link JDBCStateRepository}.
     */
    public static class Builder {

        private DataSource dataSource;
        private String tableName = "TOGGLZ";
        private MapSerializer serializer = DefaultMapSerializer.multiline();
        private boolean noCommit = false;
        private boolean createTable = true;

        /**
         * Creates a new builder for creating a {@link JDBCStateRepository}.
         *
         * @param dataSource the {@link DataSource} Togglz should use to obtain JDBC connections
         */
        public Builder(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        /**
         * Sets the table name to use for the Togglz feature state table. The default name is <code>TOGGLZ</code>.
         *
         * @param tableName The database table name
         */
        public Builder tableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        /**
         * The {@link MapSerializer} for storing parameters. By default the repository will use
         * {@link DefaultMapSerializer#multiline()}.
         *
         * @param serializer The serializer to use
         */
        public Builder serializer(MapSerializer serializer) {
            this.serializer = serializer;
            return this;
        }

        /**
         * Can be used to suppress to commit after modifying data in the repository. Can be useful if Togglz uses managed
         * connections provided by a JEE container. The default is <code>false</code>.
         *
         * @param noCommit <code>true</code> to suppress commits
         */
        public Builder noCommit(boolean noCommit) {
            this.noCommit = noCommit;
            return this;
        }

        /**
         * If set to <code>true</code>, the table will be automatically created if it is missing. The default is
         * <code>true</code>.
         *
         * @param createTable <code>true</code> if the table should be created automatically
         */
        public Builder createTable(boolean createTable) {
            this.createTable = createTable;
            return this;
        }

        /**
         * Creates a {@link JDBCStateRepository} from the current configuration
         */
        public JDBCStateRepository build() {
            return new JDBCStateRepository(this);
        }

    }

}
