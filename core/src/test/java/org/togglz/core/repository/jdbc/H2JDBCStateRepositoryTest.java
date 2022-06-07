package org.togglz.core.repository.jdbc;

import javax.sql.DataSource;
import org.h2.jdbcx.JdbcConnectionPool;

class H2JDBCStateRepositoryTest extends JDBCStateRepositoryTest {

    @Override
    protected DataSource createDataSource() {
        return JdbcConnectionPool.create("jdbc:h2:mem:", "sa", "");
    }
}
