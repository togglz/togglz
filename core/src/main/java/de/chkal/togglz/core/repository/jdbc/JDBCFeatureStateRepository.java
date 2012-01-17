package de.chkal.togglz.core.repository.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.chkal.togglz.core.Feature;
import de.chkal.togglz.core.manager.FeatureState;
import de.chkal.togglz.core.repository.FeatureStateRepository;
import de.chkal.togglz.core.util.DbUtils;

/**
 *  CREATE TABLE TOGGLZ (FEATURE CHAR(100), ENABLED INTEGER, USERS CHAR(2000))
 */
public class JDBCFeatureStateRepository implements FeatureStateRepository {

    private static final String GET_STATE_QUERY = "SELECT ENABLED FROM TOGGLZ WHERE FEATURE = ?";
    private static final String SET_STATE_UPDATE = "UPDATE TOGGLZ SET ENABLED = ? WHERE FEATURE = ?";
    private static final String SET_STATE_INSERT = "INSERT INTO TOGGLZ (FEATURE,ENABLED) VALUES (?,?)";

    private final Logger log = LoggerFactory.getLogger(JDBCFeatureStateRepository.class);

    private final DataSource dataSource;

    public JDBCFeatureStateRepository(DataSource dataSource) {
        this.dataSource = dataSource;
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
                            return new FeatureState(feature, enabled);
                        }

                    } finally {
                        DbUtils.closeQuitly(resultSet);
                    }

                } finally {
                    DbUtils.closeQuitly(statement);
                }

            } finally {
                DbUtils.closeQuitly(connection);
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
                    updateStatement.setString(2, featureState.getFeature().name());
                    updatedRows = updateStatement.executeUpdate();

                } finally {
                    DbUtils.closeQuitly(updateStatement);
                }


                /*
                 * If our update didn't modify any data we have to insert a new row
                 */
                if (updatedRows == 0) {

                    PreparedStatement insertStatement = connection.prepareStatement(SET_STATE_INSERT);
                    try {

                        insertStatement.setString(1, featureState.getFeature().name());
                        insertStatement.setInt(2, featureState.isEnabled() ? 1 : 0);
                        insertStatement.executeUpdate();

                    } finally {
                        DbUtils.closeQuitly(insertStatement);
                    }

                }

            } finally {
                DbUtils.closeQuitly(connection);
            }

        } catch (SQLException e) {
            log.error("Failed", e);
        }

     }

}
