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
import org.togglz.core.repository.util.MapSerializer;
import org.togglz.core.util.DbUtils;
import org.togglz.core.util.Strings;

/**
 * Class that creates and migrates the database table required by {@link JDBCStateRepository}.
 * 
 * @author Christian Kaltepoth
 */
class SchemaUpdater {

    private final Connection connection;

    private final String tableName;

    private final MapSerializer serializer;

    protected SchemaUpdater(Connection connection, String tableName, MapSerializer serializer) {
        this.connection = connection;
        this.tableName = tableName;
        this.serializer = serializer;
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
            statement
                .executeUpdate(insertTableName(
                "CREATE TABLE %TABLE% (FEATURE_NAME CHAR(100) PRIMARY KEY, FEATURE_ENABLED INTEGER, FEATURE_USERS CHAR(2000))"));
        } finally {
            DbUtils.closeQuietly(statement);
        }
    }

    protected boolean isSchemaVersion1() throws SQLException {

        DatabaseMetaData metaData = connection.getMetaData();
        String catalog = connection.getCatalog();

        // build a set of columns of the table
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
        return columns.contains(Columns.FEATURE_NAME) && !columns.contains(Columns.STRATEGY_ID);

    }

    protected void migrateToVersion2() throws SQLException {

        /*
         * step 1: add new columns
         */
        Statement addColumnsStmt = connection.createStatement();
        try {
            addColumnsStmt.executeUpdate(insertTableName(
                "ALTER TABLE %TABLE% ADD COLUMN STRATEGY_ID VARCHAR(200)"));
            addColumnsStmt.executeUpdate(insertTableName(
                "ALTER TABLE %TABLE% ADD COLUMN STRATEGY_PARAMS VARCHAR(2000)"));
        } finally {
            DbUtils.closeQuietly(addColumnsStmt);
        }

        /*
         * step 2: migrate the existing data
         */
        Statement updateDataStmt = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
        try {

            ResultSet resultSet = null;
            try {

                resultSet = updateDataStmt.executeQuery(insertTableName(
                    "SELECT FEATURE_NAME, FEATURE_USERS, STRATEGY_ID, STRATEGY_PARAMS FROM %TABLE%"));

                while (resultSet.next()) {

                    // migration is only required if there is data in the users column
                    String users = resultSet.getString(Columns.FEATURE_USERS);
                    if (Strings.isNotBlank(users)) {

                        // convert the user list to the new parameters format
                        Map<String, String> params = new HashMap<String, String>();
                        params.put(UsernameActivationStrategy.PARAM_USERS, users);
                        String paramData = serializer.serialize(params);
                        resultSet.updateString(Columns.STRATEGY_PARAMS, paramData);

                        // only overwrite strategy ID if it is not set yet
                        String strategyId = resultSet.getString(Columns.STRATEGY_ID);
                        if (Strings.isBlank(strategyId)) {
                            resultSet.updateString(Columns.STRATEGY_ID, UsernameActivationStrategy.ID);
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
         * step 3: remove the deprecated column
         */
        Statement removeUsersColumnStmt = connection.createStatement();
        try {
            removeUsersColumnStmt.executeUpdate(insertTableName(
                "ALTER TABLE %TABLE% DROP COLUMN FEATURE_USERS"));
        } finally {
            DbUtils.closeQuietly(removeUsersColumnStmt);
        }

    }

    private String insertTableName(String s) {
        return s.replace("%TABLE%", tableName);
    }

}
