package org.togglz.servlet.test.repository.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.util.DbUtils;
import org.togglz.test.Deployments;
import org.togglz.test.Packaging;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class JDBCRepositoryTest {

    @Deployment
    public static WebArchive createDeployment() {
        return Deployments.getBasicWebArchive()
            .addClass(JDBCRepositoryConfiguration.class)
            .addClass(JDBCFeatures.class)
            .setWebXML(Packaging.webAppDescriptor()
                .contextParam(TogglzConfig.class.getName(), JDBCRepositoryConfiguration.class.getName())
                .exportAsAsset());
    }

    @Resource(mappedName = "jboss/datasources/ExampleDS")
    private DataSource dataSource;

    @Before
    public void resetDatabase() {
        executeUpdate("DELETE FROM MYTABLE");
    }

    @Test
    public void testGetFeatureStateFromJDBCRepository() {

        FeatureManager featureManager = FeatureContext.getFeatureManager();

        assertNotNull(featureManager);
        assertNotNull(dataSource);

        FeatureState stateNoEntry = featureManager.getFeatureState(JDBCFeatures.F1);
        assertFalse(stateNoEntry.isEnabled());
        assertNull(stateNoEntry.getStrategyId());
        assertEquals(0, stateNoEntry.getParameterNames().size());

        int inserted = executeUpdate("INSERT INTO MYTABLE " +
            "(FEATURE_NAME, FEATURE_ENABLED, STRATEGY_ID, STRATEGY_PARAMS) " +
            "VALUES ('F1', 1, 'SomeStrategy', 'param=foobar')");
        assertEquals(1, inserted);

        FeatureState stateEnabled = featureManager.getFeatureState(JDBCFeatures.F1);
        assertTrue(stateEnabled.isEnabled());
        assertEquals("SomeStrategy", stateEnabled.getStrategyId());
        assertEquals(1, stateEnabled.getParameterNames().size());
        assertEquals("foobar", stateEnabled.getParameter("param"));

        executeUpdate("UPDATE MYTABLE " +
            "SET FEATURE_ENABLED = 0, STRATEGY_ID = NULL, STRATEGY_PARAMS = NULL " +
            "WHERE FEATURE_NAME = 'F1'");

        FeatureState stateDisabled = featureManager.getFeatureState(JDBCFeatures.F1);
        assertFalse(stateDisabled.isEnabled());
        assertNull(stateDisabled.getStrategyId());
        assertEquals(0, stateDisabled.getParameterNames().size());

    }

    @Test
    public void testSetFeatureStateFromJDBCRepository() {

        FeatureManager featureManager = FeatureContext.getFeatureManager();

        assertNotNull(featureManager);
        assertNotNull(dataSource);

        assertEquals(0L, executeQuery("SELECT COUNT(*) FROM MYTABLE WHERE FEATURE_NAME = 'F2'"));

        FeatureState firstState = new FeatureState(JDBCFeatures.F2, true);
        firstState.setStrategyId("someId");
        firstState.setParameter("param", "foo");
        featureManager.setFeatureState(firstState);

        assertEquals(1, executeQuery("SELECT FEATURE_ENABLED FROM MYTABLE WHERE FEATURE_NAME = 'F2'"));
        assertEquals("someId", executeQuery("SELECT STRATEGY_ID FROM MYTABLE WHERE FEATURE_NAME = 'F2'"));
        assertEquals("param=foo", executeQuery("SELECT STRATEGY_PARAMS FROM MYTABLE WHERE FEATURE_NAME = 'F2'"));

        FeatureState secondState = new FeatureState(JDBCFeatures.F2, false);
        secondState.setStrategyId(null);
        featureManager.setFeatureState(secondState);

        assertEquals(0, executeQuery("SELECT FEATURE_ENABLED FROM MYTABLE WHERE FEATURE_NAME = 'F2'"));
        assertNull(executeQuery("SELECT STRATEGY_ID FROM MYTABLE WHERE FEATURE_NAME = 'F2'"));
        assertNull(executeQuery("SELECT STRATEGY_PARAMS FROM MYTABLE WHERE FEATURE_NAME = 'F2'"));
    }

    private Object executeQuery(String sql) {

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

    private int executeUpdate(String sql) {

        try {

            Connection connection = dataSource.getConnection();
            try {

                Statement statement = connection.createStatement();
                try {

                    return statement.executeUpdate(sql);

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

}
