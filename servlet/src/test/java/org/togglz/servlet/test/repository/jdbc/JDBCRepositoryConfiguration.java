package org.togglz.servlet.test.repository.jdbc;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.togglz.core.Feature;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.jdbc.JDBCStateRepository;
import org.togglz.core.user.NoOpUserProvider;
import org.togglz.core.user.UserProvider;

public class JDBCRepositoryConfiguration implements TogglzConfig {

    @Override
    public Class<? extends Feature> getFeatureClass() {
        return JDBCFeatures.class;
    }

    @Override
    public StateRepository getStateRepository() {

        try {

            InitialContext context = new InitialContext();
            DataSource dataSource = (DataSource) context.lookup("jboss/datasources/ExampleDS");
            return new JDBCStateRepository(dataSource, "MYTABLE");

        } catch (NamingException e) {
            throw new IllegalArgumentException("Could not find datasource");
        }

    }

    @Override
    public UserProvider getUserProvider() {
        return new NoOpUserProvider();
    }

}
