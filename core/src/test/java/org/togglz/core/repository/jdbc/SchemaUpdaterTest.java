package org.togglz.core.repository.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.togglz.core.activation.UsernameActivationStrategy;
import org.togglz.core.repository.util.DefaultMapSerializer;
import org.togglz.core.util.DbUtils;

import static org.junit.jupiter.api.Assertions.*;

class SchemaUpdaterTest {

    @Test
    void shouldDetectMissingTable() throws SQLException {
        Connection connection = createConnection();
        try {
            SchemaUpdater updater = new SchemaUpdater(connection, "TOGGLZ", DefaultMapSerializer.multiline());
            assertFalse(updater.doesTableExist());

        } finally {
            DbUtils.closeQuietly(connection);
        }

    }

    @Test
    void shouldMigrateToVersion1() throws SQLException {
        Connection connection = createConnection();
        try {

            SchemaUpdater updater = new SchemaUpdater(connection, "TOGGLZ", DefaultMapSerializer.multiline());
            assertFalse(updater.doesTableExist());

            updater.migrateToVersion1();

            assertTrue(updater.doesTableExist());
            assertTrue(querySucceeds(connection, "SELECT FEATURE_NAME FROM TOGGLZ"));

        } finally {
            DbUtils.closeQuietly(connection);
        }
    }

    @Test
    void shouldDetectVersion1() throws SQLException {
        Connection connection = createConnection();
        try {

            SchemaUpdater updater = new SchemaUpdater(connection, "TOGGLZ", DefaultMapSerializer.multiline());
            assertFalse(updater.doesTableExist());

            assertFalse(updater.isSchemaVersion1());

            updater.migrateToVersion1();

            assertTrue(updater.isSchemaVersion1());

        } finally {
            DbUtils.closeQuietly(connection);
        }
    }

    @Test
    void shouldMigrateToVersion2() throws SQLException {
        Connection connection = createConnection();
        try {

            // create schema version 1
            SchemaUpdater updater = new SchemaUpdater(connection, "TOGGLZ", DefaultMapSerializer.multiline());
            assertFalse(updater.doesTableExist());
            updater.migrateToVersion1();
            assertTrue(updater.isSchemaVersion1());

            // insert two feature states
            update(connection, "INSERT INTO TOGGLZ VALUES ('F1', 1, 'ck, admin')");
            update(connection, "INSERT INTO TOGGLZ VALUES ('F2', 1, '')");
            update(connection, "INSERT INTO TOGGLZ VALUES ('F3', 1, NULL)");

            List<Object[]> dataBefore = query(connection,
                "SELECT FEATURE_NAME, FEATURE_USERS FROM TOGGLZ ORDER BY FEATURE_NAME");
            assertEquals(3, dataBefore.size());
            assertEquals("F1", dataBefore.get(0)[0]);
            assertEquals("ck, admin", dataBefore.get(0)[1]);

            // migrate the schema
            updater.migrateToVersion2();

            // check the new columns are present
            assertTrue(querySucceeds(connection, "SELECT FEATURE_NAME,STRATEGY_ID,STRATEGY_PARAMS FROM TOGGLZ"));

            // check the old users column is deleted
            assertFalse(querySucceeds(connection, "SELECT FEATURE_USERS FROM TOGGLZ"));

            // check 3 features are there after the migration
            List<Object[]> dataAfter = query(connection,
                "SELECT FEATURE_NAME, STRATEGY_ID, STRATEGY_PARAMS FROM TOGGLZ ORDER BY FEATURE_NAME");
            assertEquals(3, dataBefore.size());

            // first feature is migrated
            assertEquals("F1", dataAfter.get(0)[0]);
            assertEquals(UsernameActivationStrategy.ID, dataAfter.get(0)[1]);
            assertEquals("users=ck, admin", dataAfter.get(0)[2].toString().trim());

            // second feature didn't change
            assertEquals("F2", dataAfter.get(1)[0]);
            assertNull(dataAfter.get(1)[1]);
            assertNull(dataAfter.get(1)[2]);

            // second feature didn't change
            assertEquals("F3", dataAfter.get(2)[0]);
            assertNull(dataAfter.get(2)[1]);
            assertNull(dataAfter.get(2)[2]);

        } finally {
            DbUtils.closeQuietly(connection);
        }
    }

    private void update(Connection connection, String sql) throws SQLException {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.executeUpdate(sql);
        } finally {
            DbUtils.closeQuietly(statement);
        }
    }

    private List<Object[]> query(Connection connection, String sql) throws SQLException {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            ResultSet resultSet = null;
            try {
                resultSet = statement.executeQuery(sql);
                List<Object[]> result = new ArrayList<>();
                while (resultSet.next()) {
                    List<Object> row = new ArrayList<Object>();
                    for (int i = 0; i < resultSet.getMetaData().getColumnCount(); i++) {
                        row.add(resultSet.getObject(i + 1));
                    }
                    result.add(row.toArray());
                }
                return result;
            } finally {
                DbUtils.closeQuietly(resultSet);
            }
        } finally {
            DbUtils.closeQuietly(statement);
        }
    }

    private boolean querySucceeds(Connection connection, String sql) {
        try {
            query(connection, sql);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:h2:mem:", "sa", "");
    }

}
