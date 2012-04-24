package org.togglz.test.repository.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.sql.DataSource;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.util.DbUtils;
import org.togglz.test.Deployments;

//@RunWith(Arquillian.class)
public class JDBCRepositoryTest {

    @Deployment
    public static WebArchive createDeployment() {
        return Deployments.getCDIArchive()
                .addClass(JDBCRepositoryConfiguration.class)
                .addClass(JDBCFeatures.class);
    }

    @Inject
    private FeatureManager featureManager;

    @Resource(mappedName = "jboss/datasources/ExampleDS")
    private DataSource dataSource;

    @Test
    @Ignore
    public void testGetFeatureStateFromJDBCRepository() throws IOException {

        assertNotNull(featureManager);
        assertNotNull(dataSource);

        FeatureState stateNoEntry = featureManager.getFeatureState(JDBCFeatures.F1);
        assertEquals(false, stateNoEntry.isEnabled());
        assertEquals(0, stateNoEntry.getUsers().size());

        executeUpdate("INSERT INTO MYTABLE (FEATURE_NAME, FEATURE_ENABLED, FEATURE_USERS) VALUES ('F1', 1, 'A,B')");

        FeatureState stateEnabled = featureManager.getFeatureState(JDBCFeatures.F1);
        assertEquals(true, stateEnabled.isEnabled());
        assertEquals(2, stateEnabled.getUsers().size());
        assertEquals("A", stateEnabled.getUsers().get(0));
        assertEquals("B", stateEnabled.getUsers().get(1));

        executeUpdate("UPDATE MYTABLE SET FEATURE_ENABLED = 0, FEATURE_USERS = 'A, X, Y'  WHERE FEATURE_NAME = 'F1'");

        FeatureState stateDisabled = featureManager.getFeatureState(JDBCFeatures.F1);
        assertEquals(false, stateDisabled.isEnabled());
        assertEquals(3, stateDisabled.getUsers().size());
        assertEquals("A", stateDisabled.getUsers().get(0));
        assertEquals("X", stateDisabled.getUsers().get(1));
        assertEquals("Y", stateDisabled.getUsers().get(2));

    }

    @Test @Ignore
    public void testSetFeatureStateFromJDBCRepository() throws IOException {

        assertNotNull(featureManager);
        assertNotNull(dataSource);

        assertEquals(0l, executeQuery("SELECT COUNT(*) FROM MYTABLE WHERE FEATURE_NAME = 'F2'"));

        featureManager.setFeatureState(new FeatureState(JDBCFeatures.F2, true, Arrays.asList("A", "B")));

        assertEquals(1, executeQuery("SELECT FEATURE_ENABLED FROM MYTABLE WHERE FEATURE_NAME = 'F2'"));
        assertEquals("A, B", executeQuery("SELECT FEATURE_USERS FROM MYTABLE WHERE FEATURE_NAME = 'F2'"));

        featureManager.setFeatureState(new FeatureState(JDBCFeatures.F2, false, Arrays.asList("X")));

        assertEquals(0, executeQuery("SELECT FEATURE_ENABLED FROM MYTABLE WHERE FEATURE_NAME = 'F2'"));
        assertEquals("X", executeQuery("SELECT FEATURE_USERS FROM MYTABLE WHERE FEATURE_NAME = 'F2'"));

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
