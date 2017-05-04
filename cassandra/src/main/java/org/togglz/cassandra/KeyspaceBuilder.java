package org.togglz.cassandra;

import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.CountingConnectionPoolMonitor;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;

import static com.netflix.astyanax.connectionpool.NodeDiscoveryType.RING_DESCRIBE;

/**
 * <p>
 * Builder for a Cassandra {@link Keyspace}.
 * </p>
 * 
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>
 * Keyspace keyspace = new KeyspaceBuilder("Cluster Name", "Keyspace name")
 *     .setHosts(&quot;host1,host2,host3&quot;)
 *     .setThriftPort(9260)
 *     .build();
 * </pre>
 * 
 * @author artur@callfire.com
 */
public class KeyspaceBuilder {

    private final String clusterName;
    private final String keyspaceName;

    private String hosts = "localhost";
    private int thriftPort = 9160;

    /**
     * Creates a {@link org.togglz.cassandra.KeyspaceBuilder}.
     *
     * @param clusterName Cassandra cluster name to use
     * @param keyspaceName Cassandra keyspace name to use
     */
    public KeyspaceBuilder(final String clusterName, final String keyspaceName) {
        this.clusterName = clusterName;
        this.keyspaceName = keyspaceName;
    }

    /**
     * Sets a <code>hostnames</code> list to use. The default is <code>localhost</code>.
     *
     * @param seedHosts comma separated Cassandra cluster hosts list
     */
    public KeyspaceBuilder setHosts(final String seedHosts) {
        this.hosts = seedHosts;
        return this;
    }

    /**
     * Sets a <code>thrift port</code> to use. The default is <code>9160</code>.
     *
     * @param port thrift port
     */
    public KeyspaceBuilder setThriftPort(final int port) {
        this.thriftPort = port;
        return this;
    }

    /**
     * Builds a {@link Keyspace} from current configuration.
     */
    public Keyspace build() {
        final AstyanaxContext<Keyspace> context = new AstyanaxContext.Builder()
            .forCluster(clusterName)
            .forKeyspace(keyspaceName)
            .withAstyanaxConfiguration(new AstyanaxConfigurationImpl()
                .setDiscoveryType(RING_DESCRIBE)
            )
            .withConnectionPoolConfiguration(new ConnectionPoolConfigurationImpl("ConnectionPool")
                .setPort(thriftPort)
                .setSeeds(hosts)
            )
            .withConnectionPoolMonitor(new CountingConnectionPoolMonitor())
            .buildKeyspace(ThriftFamilyFactory.getInstance());

        context.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public synchronized void run() {
                context.shutdown();
            }
        });

        return context.getClient();
    }
}
