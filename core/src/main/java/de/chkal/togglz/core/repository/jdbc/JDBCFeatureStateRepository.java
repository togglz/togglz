package de.chkal.togglz.core.repository.jdbc;

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

import de.chkal.togglz.core.Feature;
import de.chkal.togglz.core.manager.FeatureState;
import de.chkal.togglz.core.repository.FeatureStateRepository;
import de.chkal.togglz.core.util.DbUtils;
import de.chkal.togglz.core.util.Strings;

/**
 * CREATE TABLE TOGGLZ (FEATURE CHAR(100), ENABLED INTEGER, USERS CHAR(2000))
 */
public class JDBCFeatureStateRepository implements FeatureStateRepository {

    private static final String TABLE_DDL = "CREATE TABLE TOGGLZ (FEATURE_NAME CHAR(100), FEATURE_ENABLED INTEGER, FEATURE_USERS CHAR(2000))";

    private static final String TABLE_NAME = "TOGGLZ";

    private static final String GET_STATE_QUERY = "SELECT FEATURE_ENABLED, FEATURE_USERS FROM TOGGLZ WHERE FEATURE_NAME = ?";
    private static final String SET_STATE_UPDATE = "UPDATE TOGGLZ SET FEATURE_ENABLED = ?, FEATURE_USERS = ? WHERE FEATURE_NAME = ?";
    private static final String SET_STATE_INSERT = "INSERT INTO TOGGLZ (FEATURE_NAME, FEATURE_ENABLED, FEATURE_USERS) VALUES (?,?,?)";

    private final Logger log = LoggerFactory.getLogger(JDBCFeatureStateRepository.class);

    private final DataSource dataSource;

    public JDBCFeatureStateRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        init();
    }

    private void init() {

        try {

            Connection connection = dataSource.getConnection();
            try {

                boolean togglzTableExists = true;

                DatabaseMetaData metaData = connection.getMetaData();
                String catalog = connection.getCatalog();

                ResultSet resultSet = metaData.getTables(catalog, null, TABLE_NAME, new String[] { "TABLE" });
                try {
                    togglzTableExists = resultSet.next();
                } finally {
                    DbUtils.closeQuietly(resultSet);
                }

                if (!togglzTableExists) {

                    Statement statement = connection.createStatement();
                    try {

                        statement.executeUpdate(TABLE_DDL);

                        log.info("Database table {} has been created successfully", TABLE_NAME);

                    } finally {
                        DbUtils.closeQuietly(statement);
                    }

                } else {
                    log.debug("Found existing table {} in database.", TABLE_NAME);
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

                PreparedStatement statement = connection.prepareStatement(GET_STATE_QUERY);
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
                PreparedStatement updateStatement = connection.prepareStatement(SET_STATE_UPDATE);
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

                    PreparedStatement insertStatement = connection.prepareStatement(SET_STATE_INSERT);
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
