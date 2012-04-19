package org.togglz.core.repository.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.FeatureStateRepository;
import org.togglz.core.util.DbUtils;
import org.togglz.core.util.Strings;


/**
 * <p>
 * This repository implementation can be used to store the feature state in SQL database using the standard JDBC API.
 * </p>
 * 
 * <p>
 * {@link JDBCFeatureStateRepository} stores the feature state in a single database table. You can choose the name of this table
 * using an constructor argument. If the repository doesn't find the required table in the database, it will automatically
 * create it.
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
 *   FEATURE_USERS CHAR(2000)
 * )
 * </pre>
 * 
 * @author Christian Kaltepoth
 * 
 */
public class JDBCFeatureStateRepository implements FeatureStateRepository {

    private static final String TABLE_DDL = "CREATE TABLE %TABLE% (FEATURE_NAME CHAR(100) PRIMARY KEY, FEATURE_ENABLED INTEGER, FEATURE_USERS CHAR(2000))";

    private static final String GET_STATE_QUERY = "SELECT FEATURE_ENABLED, FEATURE_USERS FROM %TABLE% WHERE FEATURE_NAME = ?";
    private static final String SET_STATE_UPDATE = "UPDATE %TABLE% SET FEATURE_ENABLED = ?, FEATURE_USERS = ? WHERE FEATURE_NAME = ?";
    private static final String SET_STATE_INSERT = "INSERT INTO %TABLE% (FEATURE_NAME, FEATURE_ENABLED, FEATURE_USERS) VALUES (?,?,?)";

    private final Logger log = LoggerFactory.getLogger(JDBCFeatureStateRepository.class);

    private final DataSource dataSource;

    private final String tableName;

    /**
     * Constructor of {@link JDBCFeatureStateRepository}. Using this constructor will automatically set the database table name
     * to <code>TOGGLZ</CODE>.
     * 
     * @param dataSource The JDBC {@link DataSource} to obtain connections from
     * @see #JDBCFeatureStateRepository(DataSource, String)
     */
    public JDBCFeatureStateRepository(DataSource dataSource) {
        this(dataSource, "TOGGLZ");
    }

    /**
     * Constructor of {@link JDBCFeatureStateRepository}.
     * 
     * @param dataSource The JDBC {@link DataSource} to obtain connections from
     * @param tableName The name of the database table to use
     */
    public JDBCFeatureStateRepository(DataSource dataSource, String tableName) {
        this.dataSource = dataSource;
        this.tableName = tableName;
        init();
    }

    private void init() {

        try {

            Connection connection = dataSource.getConnection();
            try {

                boolean togglzTableExists = true;

                DatabaseMetaData metaData = connection.getMetaData();
                String catalog = connection.getCatalog();

                ResultSet resultSet = metaData.getTables(catalog, null, tableName, new String[] { "TABLE" });
                try {
                    togglzTableExists = resultSet.next();
                } finally {
                    DbUtils.closeQuietly(resultSet);
                }

                if (!togglzTableExists) {

                    Statement statement = connection.createStatement();
                    try {

                        statement.executeUpdate(insertTableName(TABLE_DDL));

                        log.info("Database table {} has been created successfully", tableName);

                    } finally {
                        DbUtils.closeQuietly(statement);
                    }

                } else {
                    log.debug("Found existing table {} in database.", tableName);
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

                PreparedStatement statement = connection.prepareStatement(insertTableName(GET_STATE_QUERY));
                try {

                    statement.setString(1, feature.name());

                    ResultSet resultSet = statement.executeQuery();
                    try {

                        if (resultSet.next()) {

                            boolean enabled = resultSet.getInt(1) > 0;
                            List<String> users = parseUserList(resultSet.getString(2));

                            return new FeatureState(feature, enabled, users);
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
                PreparedStatement updateStatement = connection.prepareStatement(insertTableName(SET_STATE_UPDATE));
                try {

                    updateStatement.setInt(1, featureState.isEnabled() ? 1 : 0);
                    updateStatement.setString(2, createUserList(featureState.getUsers()));
                    updateStatement.setString(3, featureState.getFeature().name());
                    updatedRows = updateStatement.executeUpdate();

                } finally {
                    DbUtils.closeQuietly(updateStatement);
                }

                /*
                 * If our update didn't modify any data we have to insert a new row
                 */
                if (updatedRows == 0) {

                    PreparedStatement insertStatement = connection.prepareStatement(insertTableName(SET_STATE_INSERT));
                    try {

                        insertStatement.setString(1, featureState.getFeature().name());
                        insertStatement.setInt(2, featureState.isEnabled() ? 1 : 0);
                        insertStatement.setString(3, createUserList(featureState.getUsers()));
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

    private String createUserList(List<String> users) {
        if (users != null && users.size() > 0) {
            return Strings.join(users, ", ");
        }
        return null;
    }

    private List<String> parseUserList(String str) {
        List<String> result = new ArrayList<String>();
        if (Strings.isNotBlank(str)) {
            for (String u : str.split("[,\\s]+")) {
                if (u != null && u.trim().length() > 0) {
                    result.add(u.trim());
                }
            }
        }
        return result;
    }

}
