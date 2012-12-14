package org.togglz.core.repository.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.togglz.core.Feature;
import org.togglz.core.logging.Log;
import org.togglz.core.logging.LogFactory;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.util.DbUtils;
import org.togglz.core.util.MapConverter;
import org.togglz.core.util.Strings;

/**
 * <p>
 * This repository implementation can be used to store the feature state in SQL database using the standard JDBC API.
 * </p>
 * 
 * <p>
 * {@link JDBCStateRepository} stores the feature state in a single database table. You can choose the name of this table using
 * an constructor argument. If the repository doesn't find the required table in the database, it will automatically create it.
 * </p>
 * 
 * <p>
 * The database table has the following format:
 * </p>
 * 
 * <pre>
 * CREATE TABLE &lt;table&gt; (
 *   FEATURE_NAME CHAR(100) PRIMARY KEY, 
 *   FEATURE_ENABLED INTEGER, 
 *   STRATEGY_ID CHAR(200), 
 *   STRATEGY_PARAMS CHAR(2000)
 * )
 * </pre>
 * 
 * @author Christian Kaltepoth
 * 
 */
public class JDBCStateRepository implements StateRepository {

    protected static final String COLUMN_FEATURE_ENABLED = "FEATURE_ENABLED";
    protected static final String COLUMN_STRATEGY_ID = "STRATEGY_ID";
    protected static final String COLUMN_STRATEGY_PARAMS = "STRATEGY_PARAMS";

    private final Log log = LogFactory.getLog(JDBCStateRepository.class);

    private final DataSource dataSource;

    private final String tableName;

    private final MapConverter mapConverter;

    /**
     * Constructor of {@link JDBCStateRepository}. Using this constructor will automatically set the database table name to
     * <code>TOGGLZ</CODE>.
     * 
     * @param dataSource The JDBC {@link DataSource} to obtain connections from
     * @see #JDBCFeatureStateRepository(DataSource, String)
     */
    public JDBCStateRepository(DataSource dataSource) {
        this(dataSource, "TOGGLZ");
    }

    /**
     * Constructor of {@link JDBCStateRepository}.
     * 
     * @param dataSource The JDBC {@link DataSource} to obtain connections from
     * @param tableName The name of the database table to use
     */
    public JDBCStateRepository(DataSource dataSource, String tableName) {
        this(dataSource, tableName, MapConverter.create().withNewLines());
    }

    /**
     * Constructor of {@link JDBCStateRepository}.
     * 
     * @param dataSource The JDBC {@link DataSource} to obtain connections from
     * @param tableName The name of the database table to use
     * @param mapConverter The {@link MapConverter} instance to use
     */
    public JDBCStateRepository(DataSource dataSource, String tableName, MapConverter mapConverter) {
        this.dataSource = dataSource;
        this.tableName = tableName;
        this.mapConverter = mapConverter;
        init();
    }

    /**
     * Method for creating/migrating the database schema
     */
    protected void init() {

        try {

            Connection connection = dataSource.getConnection();
            try {

                SchemaUpdater updater = new SchemaUpdater(connection, tableName, mapConverter);
                if (!updater.doesTableExist()) {
                    updater.migrateToVersion1();
                }
                if (updater.isSchemaVersion1()) {
                    updater.migrateToVersion2();
                }

            } finally {
                DbUtils.closeQuietly(connection);
            }

        } catch (SQLException e) {
            log.error("Failed", e);
        }

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

                            boolean enabled = resultSet.getInt(COLUMN_FEATURE_ENABLED) > 0;
                            FeatureState state = new FeatureState(feature, enabled);

                            String strategyId = resultSet.getString(COLUMN_STRATEGY_ID);
                            if (Strings.isNotBlank(strategyId)) {
                                state.setStrategyId(strategyId.trim());
                            }

                            String paramsAsString = resultSet.getString(COLUMN_STRATEGY_PARAMS);
                            if (Strings.isNotBlank(paramsAsString)) {
                                Map<String, String> params = mapConverter.convertFromString(paramsAsString);
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
            log.error("Failed", e);
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

                    updateStatement.setInt(1, featureState.isEnabled() ? 1 : 0);
                    updateStatement.setString(2, Strings.trimToNull(featureState.getStrategyId()));
                    String paramsAsString = mapConverter.convertToString(featureState.getParameterMap());
                    updateStatement.setString(3, Strings.trimToNull(paramsAsString));
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

                        insertStatement.setString(1, featureState.getFeature().name());
                        insertStatement.setInt(2, featureState.isEnabled() ? 1 : 0);
                        insertStatement.setString(3, Strings.trimToNull(featureState.getStrategyId()));
                        String paramsAsString = mapConverter.convertToString(featureState.getParameterMap());
                        insertStatement.setString(4, Strings.trimToNull(paramsAsString));

                        insertStatement.executeUpdate();

                    } finally {
                        DbUtils.closeQuietly(insertStatement);
                    }

                }

            } finally {
                DbUtils.closeQuietly(connection);
            }

        } catch (SQLException e) {
            log.error("Failed", e);
        }

    }

    private String insertTableName(String s) {
        return s.replace("%TABLE%", tableName);
    }

}
