package org.togglz.core.repository.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.util.DefaultMapSerializer;
import org.togglz.core.util.DbUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class JDBCStateRepositoryTest {

    private static final AtomicInteger COUNTER = new AtomicInteger();

    DataSource dataSource;
    String tableName;
    JDBCStateRepository repository;

    @BeforeEach
    void before() throws SQLException {
        dataSource = createDataSource();
        tableName = "togglz" + COUNTER.getAndIncrement();
        repository = createRepository(dataSource);
    }

    abstract DataSource createDataSource() throws SQLException;

    JDBCStateRepository.Builder defaultBuilder(DataSource dataSource) {
        return JDBCStateRepository.newBuilder(dataSource)
                .tableName(tableName)
                .createTable(true)
                .serializer(DefaultMapSerializer.multiline());
    }

    JDBCStateRepository createRepository(DataSource dataSource) {
        return defaultBuilder(dataSource).build();
    }

    @Test
    void testShouldSaveStateWithoutStrategyOrParameters() {

        /*
         * WHEN a feature without strategy is persisted
         */
        FeatureState state = new FeatureState(TestFeature.F1).disable();
        repository.setFeatureState(state);

        /*
         * THEN there should be a corresponding entry in the database
         */
        assertEquals(1L, query(dataSource, substitute("SELECT COUNT(*) FROM %TABLE% WHERE FEATURE_NAME = 'F1'")));
        assertEquals(0, query(dataSource,  substitute("SELECT FEATURE_ENABLED FROM %TABLE% WHERE FEATURE_NAME = 'F1'")));
        assertNull(query(dataSource, substitute("SELECT STRATEGY_ID FROM %TABLE% WHERE FEATURE_NAME = 'F1'")));
        assertNull(query(dataSource, substitute("SELECT STRATEGY_PARAMS FROM %TABLE% WHERE FEATURE_NAME = 'F1'")));
    }

    @Test
    void testShouldSaveStateStrategyAndParameters() {

        /*
         * WHEN a feature without strategy is persisted
         */
        FeatureState state = new FeatureState(TestFeature.F1)
            .enable()
            .setStrategyId("someId")
            .setParameter("param", "foo");
        repository.setFeatureState(state);

        /*
         * THEN there should be a corresponding entry in the database
         */
        assertEquals(1L, query(dataSource, substitute("SELECT COUNT(*) FROM %TABLE% WHERE FEATURE_NAME = 'F1'")));
        assertEquals(1, query(dataSource, substitute("SELECT FEATURE_ENABLED FROM %TABLE% WHERE FEATURE_NAME = 'F1'")));
        assertEquals("someId", query(dataSource, substitute("SELECT STRATEGY_ID FROM %TABLE% WHERE FEATURE_NAME = 'F1'")));
        assertEquals("param=foo", query(dataSource, substitute("SELECT STRATEGY_PARAMS FROM %TABLE% WHERE FEATURE_NAME = 'F1'")));
    }

    @Test
    void testShouldReadStateWithoutStrategyAndParameters() {
        /*
         * GIVEN a database row containing a simple feature state
         */
        update(dataSource, substitute("INSERT INTO %TABLE% VALUES ('F1', 0, NULL, NULL)"));

        /*
         * WHEN the repository reads the state
         */
        FeatureState state = repository.getFeatureState(TestFeature.F1);

        /*
         * THEN the properties should be set like expected
         */
        assertNotNull(state);
        assertEquals(TestFeature.F1, state.getFeature());
        assertFalse(state.isEnabled());
        assertNull(state.getStrategyId());
        assertEquals(0, state.getParameterNames().size());
    }

    @Test
    void testShouldReadStateWithStrategyAndParameters() {
        /*
         * GIVEN a database row containing a simple feature state
         */
        update(dataSource, substitute("INSERT INTO %TABLE% VALUES ('F1', 1, 'myStrategy', 'param23=foobar')"));

        /*
         * WHEN the repository reads the state
         */
        FeatureState state = repository.getFeatureState(TestFeature.F1);

        /*
         * THEN the properties should be set like expected
         */
        assertNotNull(state);
        assertEquals(TestFeature.F1, state.getFeature());
        assertTrue(state.isEnabled());
        assertEquals("myStrategy", state.getStrategyId());
        assertEquals(1, state.getParameterNames().size());
        assertEquals("foobar", state.getParameter("param23"));
    }

    @Test
    void testShouldUpdateExistingDatabaseEntry() {
        /*
         * GIVEN a database row containing a simple feature state
         */
        update(dataSource, substitute("INSERT INTO %TABLE% VALUES ('F1', 1, 'myStrategy', 'param23=foobar')"));

        /*
         * AND the database entries are like expected
         */
        assertEquals(1L, query(dataSource, substitute("SELECT COUNT(*) FROM %TABLE% WHERE FEATURE_NAME = 'F1'")));
        assertEquals(1, query(dataSource, substitute("SELECT FEATURE_ENABLED FROM %TABLE% WHERE FEATURE_NAME = 'F1'")));
        assertEquals("myStrategy", query(dataSource, substitute("SELECT STRATEGY_ID FROM %TABLE% WHERE FEATURE_NAME = 'F1'")));
        assertEquals("param23=foobar", query(dataSource, substitute("SELECT STRATEGY_PARAMS FROM %TABLE% WHERE FEATURE_NAME = 'F1'")));

        /*
         * WHEN the repository writes new state
         */
        FeatureState state = new FeatureState(TestFeature.F1)
            .disable()
            .setStrategyId("someId")
            .setParameter("param", "foo");
        repository.setFeatureState(state);

        /*
         * THEN the properties should be set like expected
         */
        assertEquals(1L, query(dataSource, substitute("SELECT COUNT(*) FROM %TABLE% WHERE FEATURE_NAME = 'F1'")));
        assertEquals(0, query(dataSource, substitute("SELECT FEATURE_ENABLED FROM %TABLE% WHERE FEATURE_NAME = 'F1'")));
        assertEquals("someId", query(dataSource, substitute("SELECT STRATEGY_ID FROM %TABLE% WHERE FEATURE_NAME = 'F1'")));
        assertEquals("param=foo", query(dataSource, substitute("SELECT STRATEGY_PARAMS FROM %TABLE% WHERE FEATURE_NAME = 'F1'")));

	}

	@Test
	void testShouldPropagateTheExceptionWhenReadFails() throws SQLException {

		/*
		 * GIVEN a database row containing a simple feature state
		 */
		update(dataSource, substitute("INSERT INTO %TABLE% VALUES ('F1', 0, NULL, NULL)"));

		/*
		 * AND the datasource throws an exception when we try to get a
		 * connection
		 */
		DataSource spyedDataSource = Mockito.spy(dataSource);
        repository = createRepository(spyedDataSource);
		Mockito.when(spyedDataSource.getConnection()).thenThrow(new SQLException("Failed to get a connection"));

		/*
		 * WHEN the repository reads the state
		 */
        assertThrows(IllegalStateException.class, () -> repository.getFeatureState(TestFeature.F1));

		/*
		 * THEN an IllegalStateException is thrown
		 */
	}

	@Test
	void testShouldPropagateTheExceptionWhenWriteFails() throws SQLException {

		/*
		 * GIVEN a feature state to persist
		 */
		FeatureState state = new FeatureState(TestFeature.F1).enable();

		/*
		 * AND the datasource throws an exception when we try to get a
		 * connection
		 */
		DataSource spyedDataSource = Mockito.spy(dataSource);
		repository = createRepository(spyedDataSource);
		Mockito.when(spyedDataSource.getConnection()).thenThrow(new SQLException("Failed to get a connection"));

		/*
		 * WHEN the feature state is persisted
		 */
        assertThrows(IllegalStateException.class, () -> repository.setFeatureState(state));
		/*
		 * THEN an IllegalStateException is thrown
		 */
    }

    Object query(DataSource dataSource, String sql) {
        try {
            Connection connection = dataSource.getConnection();
            try {
                Statement statement = connection.createStatement();
                try {
                    ResultSet resultSet = statement.executeQuery(sql);
                    try {
                        if (resultSet.next()) {
                            return resultSet.getObject(1);
                        }
                        return null;
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
            throw new IllegalStateException(e);
        }

    }

    void update(DataSource dataSource, String sql) {
        try {
            Connection connection = dataSource.getConnection();
            try {

                Statement statement = connection.createStatement();
                try {
                    statement.executeUpdate(sql);
                } finally {
                    DbUtils.closeQuietly(statement);
                }
            } finally {
                DbUtils.closeQuietly(connection);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    String substitute(String s) {
        return s.replace("%TABLE%", tableName);
    }

    private enum TestFeature implements Feature {
        F1
    }
}
