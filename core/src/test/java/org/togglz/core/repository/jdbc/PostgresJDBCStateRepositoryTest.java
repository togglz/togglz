package org.togglz.core.repository.jdbc;

import javax.sql.DataSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class PostgresJDBCStateRepositoryTest extends JDBCStateRepositoryTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:latest");


    @Override
    protected DataSource createDataSource() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setServerNames(new String[]{POSTGRES_CONTAINER.getHost()});
        dataSource.setDatabaseName(POSTGRES_CONTAINER.getDatabaseName());
        dataSource.setUser(POSTGRES_CONTAINER.getUsername());
        dataSource.setPassword(POSTGRES_CONTAINER.getPassword());
        dataSource.setPortNumbers(new int[]{POSTGRES_CONTAINER.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT)});
        return dataSource;
    }
}
