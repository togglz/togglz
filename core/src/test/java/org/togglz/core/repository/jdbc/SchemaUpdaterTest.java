package org.togglz.core.repository.jdbc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Test;
import org.togglz.core.util.DbUtils;

public class SchemaUpdaterTest {

    @Test
    public void shouldDetectMissingTable() throws SQLException {

        Connection connection = createConnection();
        try {

            SchemaUpdater updater = new SchemaUpdater(connection, "TOGGLZ");
            assertFalse(updater.doesTableExist());

        } finally {
            DbUtils.closeQuietly(connection);
        }

    }

    @Test
    public void shouldMigrateToVersion1() throws SQLException {

        Connection connection = createConnection();
        try {

            SchemaUpdater updater = new SchemaUpdater(connection, "TOGGLZ");
            assertFalse(updater.doesTableExist());

            updater.migrateToVersion1();

            assertTrue(updater.doesTableExist());
            assertTrue(querySucceeds(connection, "SELECT FEATURE_NAME FROM TOGGLZ"));

        } finally {
            DbUtils.closeQuietly(connection);
        }

    }

    @Test
    public void shouldDetectVersion1() throws SQLException {

        Connection connection = createConnection();
        try {

            SchemaUpdater updater = new SchemaUpdater(connection, "TOGGLZ");
            assertFalse(updater.doesTableExist());

            assertFalse(updater.isSchemaVersion1());

            updater.migrateToVersion1();

            assertTrue(updater.isSchemaVersion1());

        } finally {
            DbUtils.closeQuietly(connection);
        }

    }

    @Test
    public void shouldMigrateToVersion2() throws SQLException {

        Connection connection = createConnection();
        try {

            SchemaUpdater updater = new SchemaUpdater(connection, "TOGGLZ");
            assertFalse(updater.doesTableExist());
            updater.migrateToVersion1();
            assertTrue(updater.isSchemaVersion1());

            updater.migrateToVersion2();

            assertTrue(querySucceeds(connection, "SELECT FEATURE_NAME,STRATEGY_ID,STRATEGY_PARAMS FROM TOGGLZ"));

        } finally {
            DbUtils.closeQuietly(connection);
        }

    }

    private boolean querySucceeds(Connection connection, String sql) {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            try {
                return true;
            } finally {
                DbUtils.closeQuietly(resultSet);
            }
        } catch (SQLException e) {
            return false;
        } finally {
            DbUtils.closeQuietly(statement);
        }
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:h2:mem:", "sa", "");
    }

}
