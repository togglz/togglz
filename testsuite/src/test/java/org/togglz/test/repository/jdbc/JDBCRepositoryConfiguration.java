package org.togglz.test.repository.jdbc;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.sql.DataSource;

import org.togglz.core.Feature;
import org.togglz.core.config.TogglzConfig;
import org.togglz.core.repository.FeatureStateRepository;
import org.togglz.core.repository.jdbc.JDBCFeatureStateRepository;
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
    public FeatureStateRepository getFeatureStateRepository() {

        if (dataSource == null) {
            throw new IllegalStateException("No datasource found");
        }

        return new JDBCFeatureStateRepository(dataSource, "MYTABLE");

    }

    @Override
    public FeatureUserProvider getFeatureUserProvider() {
        return new NoOpFeatureUserProvider();
    }

}
