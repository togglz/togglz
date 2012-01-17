package de.chkal.togglz.test.repository.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.sql.DataSource;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.chkal.togglz.core.manager.FeatureManager;
import de.chkal.togglz.core.manager.FeatureState;
import de.chkal.togglz.core.util.DbUtils;
import de.chkal.togglz.test.Deployments;

@RunWith(Arquillian.class)
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
    public void testGetFeatureStateFromJDBCRepository() throws IOException {

        assertNotNull(featureManager);
        assertNotNull(dataSource);

        assertFalse(featureManager.isActive(JDBCFeatures.F1));

        executeUpdate("INSERT INTO TOGGLZ (FEATURE, ENABLED) VALUES ('F1', 1)");

        assertTrue(featureManager.isActive(JDBCFeatures.F1));

        executeUpdate("UPDATE TOGGLZ SET ENABLED = 0 WHERE FEATURE = 'F1'");

        assertFalse(featureManager.isActive(JDBCFeatures.F1));

    }

    @Test
    public void testSetFeatureStateFromJDBCRepository() throws IOException {

        assertNotNull(featureManager);
        assertNotNull(dataSource);

        assertEquals(null, executeQuery("SELECT ENABLED FROM TOGGLZ WHERE FEATURE = 'F2'"));

        featureManager.setFeatureState(new FeatureState(JDBCFeatures.F2, true));

        assertEquals(1, executeQuery("SELECT ENABLED FROM TOGGLZ WHERE FEATURE = 'F2'"));

        featureManager.setFeatureState(new FeatureState(JDBCFeatures.F2, false));

        assertEquals(0, executeQuery("SELECT ENABLED FROM TOGGLZ WHERE FEATURE = 'F2'"));

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
