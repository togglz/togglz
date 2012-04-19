package org.togglz.test.repository.jdbc;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.sql.DataSource;

import org.togglz.core.Feature;
import org.togglz.core.config.TogglzConfig;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.jdbc.JDBCStateRepository;
import org.togglz.core.user.FeatureUserProvider;
import org.togglz.core.user.NoOpFeatureUserProvider;


@ApplicationScoped
public class JDBCRepositoryConfiguration implements TogglzConfig {

    @Resource(mappedName = "jboss/datasources/ExampleDS")
    private DataSource dataSource;

    @Override
    public Class<? extends Feature> getFeatureClass() {
        return JDBCFeatures.class;
    }

    @Override
    public StateRepository getStateRepository() {

        if (dataSource == null) {
            throw new IllegalStateException("No datasource found");
        }

        return new JDBCStateRepository(dataSource, "MYTABLE");

    }

    @Override
    public FeatureUserProvider getFeatureUserProvider() {
        return new NoOpFeatureUserProvider();
    }

}
