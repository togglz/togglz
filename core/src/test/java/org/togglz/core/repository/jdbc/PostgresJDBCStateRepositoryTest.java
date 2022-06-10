package org.togglz.core.repository.jdbc;

import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
abstract class PostgresJDBCStateRepositoryTest extends JDBCStateRepositoryTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:latest");

    @Override
    DataSource createDataSource() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setServerNames(new String[]{POSTGRES_CONTAINER.getHost()});
        dataSource.setDatabaseName(POSTGRES_CONTAINER.getDatabaseName());
        dataSource.setUser(POSTGRES_CONTAINER.getUsername());
        dataSource.setPassword(POSTGRES_CONTAINER.getPassword());
        dataSource.setPortNumbers(new int[]{POSTGRES_CONTAINER.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT)});
        return dataSource;
    }

    String substituteColumn(String s, String columnName) {
        return s.replace("%COLUMN%", columnName);
    }

    abstract String expectedColumnType();

    @Test
    void testColumnType() {
        String sql = "SELECT data_type FROM information_schema.columns WHERE table_name = '%TABLE%' AND column_name = '%COLUMN%'";
        assertEquals(expectedColumnType(), query(dataSource, substitute(substituteColumn(sql, "feature_name"))));
        assertEquals(expectedColumnType(), query(dataSource, substitute(substituteColumn(sql, "strategy_id"))));
        assertEquals(expectedColumnType(), query(dataSource, substitute(substituteColumn(sql, "strategy_params"))));
    }
}

class DefaultPostgresJDBCStateRepositoryTest extends PostgresJDBCStateRepositoryTest {

    @Override
    String expectedColumnType() {
        return "character varying";
    }
}

class TextPostgresJDBCStateRepositoryTest extends PostgresJDBCStateRepositoryTest {

    @Override
    JDBCStateRepository createRepository(DataSource dataSource) {
        return defaultBuilder(dataSource)
                .usePostgresTextColumns(true)
                .build();
    }

    @Override
    String expectedColumnType() {
        return "text";
    }
}
