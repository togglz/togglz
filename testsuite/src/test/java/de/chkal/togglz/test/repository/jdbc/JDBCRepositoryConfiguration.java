package de.chkal.togglz.test.repository.jdbc;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.sql.DataSource;

import de.chkal.togglz.core.Feature;
import de.chkal.togglz.core.config.FeatureManagerConfiguration;
import de.chkal.togglz.core.repository.FeatureStateRepository;
import de.chkal.togglz.core.repository.jdbc.JDBCFeatureStateRepository;
import de.chkal.togglz.core.user.FeatureUserProvider;
import de.chkal.togglz.core.user.NoOpFeatureUserProvider;

@ApplicationScoped
public class JDBCRepositoryConfiguration implements FeatureManagerConfiguration {

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

        return new JDBCFeatureStateRepository(dataSource);

    }

    @Override
    public FeatureUserProvider getFeatureUserProvider() {
        return new NoOpFeatureUserProvider();
    }

}
