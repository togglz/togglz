package org.togglz.core.repository.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.togglz.core.activation.UsernameActivationStrategy;
import org.togglz.core.logging.Log;
import org.togglz.core.logging.LogFactory;
import org.togglz.core.util.DbUtils;
import org.togglz.core.util.MapConverter;
import org.togglz.core.util.Strings;

/**
 * Class that creates and migrates the database table required by {@link JDBCStateRepository}.
 * 
 * @author Christian Kaltepoth
 */
class SchemaUpdater {

    private static final String V1_CREATE_TABLE = "CREATE TABLE %TABLE% (FEATURE_NAME CHAR(100) PRIMARY KEY, FEATURE_ENABLED INTEGER, FEATURE_USERS CHAR(2000))";

    private static final String COLUMN_FEATURE_NAME = "FEATURE_NAME";
    private static final String COLUMN_FEATURE_USERS = "FEATURE_USERS";
    private static final String COLUMN_STRATEGY_ID = "STRATEGY_ID";
    private static final String COLUMN_STRATEGY_PARAMS = "STRATEGY_PARAMS";

    private final Log log = LogFactory.getLog(SchemaUpdater.class);

    private final Connection connection;

    private final String tableName;

    private final MapConverter mapConverter;

    protected SchemaUpdater(Connection connection, String tableName, MapConverter mapConverter) {
        this.connection = connection;
        this.tableName = tableName;
        this.mapConverter = mapConverter;
    }

    protected void migrate() throws SQLException {

        // schema version 1
        if (!doesTableExist()) {
            log.info("Creating initial version of Togglz database table...");
            migrateToVersion1();
        }

    }

    protected boolean doesTableExist() throws SQLException {

        DatabaseMetaData metaData = connection.getMetaData();
        String catalog = connection.getCatalog();

        ResultSet resultSet = metaData.getTables(catalog, null, tableName, new String[] { "TABLE" });
        try {
            return resultSet.next();
        } finally {
            DbUtils.closeQuietly(resultSet);
        }

    }

    protected void migrateToVersion1() throws SQLException {
        Statement statement = connection.createStatement();
        try {
            statement.executeUpdate(insertTableName(V1_CREATE_TABLE));
        } finally {
            DbUtils.closeQuietly(statement);
        }
    }

    private String insertTableName(String s) {
        return s.replace("%TABLE%", tableName);
    }

    public boolean isSchemaVersion1() throws SQLException {

        DatabaseMetaData metaData = connection.getMetaData();
        String catalog = connection.getCatalog();

        // we build a set of columns of the togglz table
        Set<String> columns = new HashSet<String>();
        ResultSet resultSet = metaData.getColumns(catalog, null, tableName, null);
        try {
            while (resultSet.next()) {
                String col = resultSet.getString("COLUMN_NAME");
                if (Strings.isNotBlank(col)) {
                    columns.add(col);
                }
            }
        } finally {
            DbUtils.closeQuietly(resultSet);
        }

        // version 1 check
        return columns.contains(COLUMN_FEATURE_NAME) && !columns.contains(COLUMN_STRATEGY_ID);

    }

    protected void migrateToVersion2() throws SQLException {

        // add the new columns
        Statement addColumnsStmt = connection.createStatement();
        try {
            addColumnsStmt.executeUpdate(insertTableName(
                "ALTER TABLE %TABLE% ADD COLUMN STRATEGY_ID CHAR(200)"));
            addColumnsStmt.executeUpdate(insertTableName(
                "ALTER TABLE %TABLE% ADD COLUMN STRATEGY_PARAMS CHAR(2000)"));
        } finally {
            DbUtils.closeQuietly(addColumnsStmt);
        }

        // migrate the existing data
        Statement updateDataStmt = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
        try {

            ResultSet resultSet = null;
            try {

                resultSet = updateDataStmt.executeQuery(insertTableName(
                    "SELECT FEATURE_NAME, FEATURE_USERS, STRATEGY_ID, STRATEGY_PARAMS FROM %TABLE%"));

                while (resultSet.next()) {

                    // migration is only required if there is data in the users column
                    String users = resultSet.getString(COLUMN_FEATURE_USERS);
                    if (Strings.isNotBlank(users)) {

                        // convert the user list to the new parameters format
                        Map<String,String> params = new HashMap<String, String>();
                        params.put(UsernameActivationStrategy.PARAM_USERS, users);
                        String paramsAsString = mapConverter.convertToString(params);
                        resultSet.updateString(COLUMN_STRATEGY_PARAMS, paramsAsString);

                        // only overwrite strategy ID if it is not set yet
                        String strategyId = resultSet.getString(COLUMN_STRATEGY_ID);
                        if (Strings.isBlank(strategyId)) {
                            resultSet.updateString(COLUMN_STRATEGY_ID, UsernameActivationStrategy.ID);
                        }

                        // perform the update
                        resultSet.updateRow();

                    }

                }

            } finally {
                DbUtils.closeQuietly(resultSet);
            }
        } finally {
            DbUtils.closeQuietly(updateDataStmt);
        }

        /*
         * remove the deprecated column
         */
        Statement removeUsersColumnStmt = connection.createStatement();
        try {
            removeUsersColumnStmt.executeUpdate(insertTableName(
                "ALTER TABLE %TABLE% DROP COLUMN FEATURE_USERS"));
        } finally {
            DbUtils.closeQuietly(removeUsersColumnStmt);
        }

    }

}
