package org.togglz.core.repository.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.util.DefaultMapSerializer;

public class JDBCRepositoryAutoCommitTest {

    private DataSource dataSource;

    private JDBCStateRepository repository;

    @Test
    public void shouldUpdateWithAutoCommitEnabled() {
        givenSomeDataSourceWithAutoCommitSetTo(true);
        whenTheFeatureIsEnabled();
        thenTheDatabaseShouldBeUpdated();
    }

    @Test
    public void shouldUpdateWithAutoCommitDisabled() {
        givenSomeDataSourceWithAutoCommitSetTo(false);
        whenTheFeatureIsEnabled();
        thenTheDatabaseShouldBeUpdated();
    }

    private void givenSomeDataSourceWithAutoCommitSetTo(boolean autoCommit) {
        String url = "jdbc:h2:mem:" + this.getClass().getSimpleName() + System.currentTimeMillis();
        JdbcConnectionPool pool = JdbcConnectionPool.create(url, "sa", "");
        dataSource = new AutoCommitTestDataSource(pool, autoCommit);
        repository = new JDBCStateRepository(dataSource, "TOGGLZ", true,
            DefaultMapSerializer.multiline());
    }

    private void whenTheFeatureIsEnabled() {
        FeatureState state = new FeatureState(AutoCommitFeature.F1)
            .enable()
            .setStrategyId("foobar");
        repository.setFeatureState(state);
    }

    private void thenTheDatabaseShouldBeUpdated() {
        FeatureState state = repository.getFeatureState(AutoCommitFeature.F1);
        assertThat(state).isNotNull();
        assertThat(state.isEnabled()).isTrue();
        assertThat(state.getStrategyId()).isEqualTo("foobar");
    }

    private static enum AutoCommitFeature implements Feature {
        F1;
    }

    private static class AutoCommitTestDataSource implements DataSource {

        private final DataSource delegate;

        private final boolean autoCommit;

        public AutoCommitTestDataSource(DataSource delegate, boolean autoCommit) {
            this.delegate = delegate;
            this.autoCommit = autoCommit;
        }

        @Override
        public PrintWriter getLogWriter() throws SQLException {
            return delegate.getLogWriter();
        }

        @Override
        public void setLogWriter(PrintWriter out) throws SQLException {
            delegate.setLogWriter(out);
        }

        @Override
        public void setLoginTimeout(int seconds) throws SQLException {
            delegate.setLoginTimeout(seconds);
        }

        @Override
        public int getLoginTimeout() throws SQLException {
            return delegate.getLoginTimeout();
        }

        // No @Override, because of interface compatibility issue
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException();
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return delegate.unwrap(iface);
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return delegate.isWrapperFor(iface);
        }

        @Override
        public Connection getConnection() throws SQLException {
            Connection connection = delegate.getConnection();
            connection.setAutoCommit(autoCommit);
            return connection;
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            throw new IllegalStateException("Should not happen in the test");
        }

    }

}
