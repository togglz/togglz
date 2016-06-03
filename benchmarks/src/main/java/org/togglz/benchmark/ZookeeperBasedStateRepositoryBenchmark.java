package org.togglz.benchmark;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.client.ZooKeeperSaslClient;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.togglz.core.repository.StateRepository;
import org.togglz.zookeeper.ZookeeperStateRepository;

/**
 * Benchmark of the in-memory state repository.
 *
 * @author Ryan Gardner
 * @date 5/31/16
 */
public class ZookeeperBasedStateRepositoryBenchmark extends AbstractStateRepositoryBenchmark {

    private TestingServer server;
    private CuratorFramework client;

    @Override
    public StateRepository initializeStateRepository() throws Exception {
        server = new TestingServer();
        System.setProperty(ZooKeeperSaslClient.ENABLE_CLIENT_SASL_KEY, "false");
        client = CuratorFrameworkFactory.builder().connectString(server.getConnectString()).retryPolicy(new RetryOneTime(2000)).build();
        client.start();

        return ZookeeperStateRepository.newBuilder(client, "/togglz/jmh-benchmark/features").build();
    }

    @Override
    public void cleanupStateRepository() throws Exception {
        client.close();
        server.stop();
        System.clearProperty(ZooKeeperSaslClient.ENABLE_CLIENT_SASL_KEY);
    }

    // run this method to execute this test
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ZookeeperBasedStateRepositoryBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }

}
