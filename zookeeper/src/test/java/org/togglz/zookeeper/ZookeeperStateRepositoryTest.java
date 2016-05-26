package org.togglz.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.client.ZooKeeperSaslClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.activation.UsernameActivationStrategy;
import org.togglz.core.repository.FeatureState;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.builder.EqualsBuilder.reflectionEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * @author Ryan Gardner
 * @date 5/26/16
 */
public class ZookeeperStateRepositoryTest {

    private ZookeeperStateRepository stateRepository;

    static class ServerClientPair {
        public final TestingServer server;
        public final CuratorFramework client;

        ServerClientPair(TestingServer server, CuratorFramework client) {
            this.server = server;
            this.client = client;
        }
    }

    public static ServerClientPair startServer(Map<String,String> data) throws Exception {

        TestingServer server = new TestingServer();
        CuratorFramework client;
        // for environments where sasl is set, null it our for our test
        System.setProperty(ZooKeeperSaslClient.ENABLE_CLIENT_SASL_KEY, "false");

        client = CuratorFrameworkFactory.builder().connectString(server.getConnectString()).retryPolicy(new RetryOneTime(2000)).build();
        client.start();

        for (Map.Entry<String,String> initialData : data.entrySet()) {
            client.create().forPath(initialData.getKey(), initialData.getValue().getBytes("UTF-8"));
        }
        return new ServerClientPair(server, client);
    }

    public static void stopServer(ServerClientPair pair) throws IOException {
        pair.client.close();
        pair.server.stop();
        System.clearProperty(ZooKeeperSaslClient.ENABLE_CLIENT_SASL_KEY);
    }

    ServerClientPair serverClientPair;


    @Before
    public void setupTest() throws Exception {
        ServerClientPair serverClientPair = startServer(new HashMap<>());
        stateRepository = ZookeeperStateRepository.newBuilder(serverClientPair.client, "/test").build();
    }

    @After
    public void cleanUp() throws Exception {
        stopServer(serverClientPair);
    }

    @Test
    public void testFeatureSavingAndLoading() {
        assertNull(stateRepository.getFeatureState(TestFeature.FEATURE));
        stateRepository.setFeatureState(new FeatureState(TestFeature.FEATURE));

        assertNotNull(stateRepository.getFeatureState(TestFeature.FEATURE));
    }

    @Test
    public void testActivationStrategySavingAndLoading() {
        FeatureState savedFeatureState = new FeatureState(TestFeature.FEATURE);
        savedFeatureState.setStrategyId(UsernameActivationStrategy.ID);
        savedFeatureState.setParameter(UsernameActivationStrategy.PARAM_USERS, "user1, user2, user3");
        stateRepository.setFeatureState(savedFeatureState);

        FeatureState loadedFeatureState = stateRepository.getFeatureState(TestFeature.FEATURE);

        assertThat(reflectionEquals(savedFeatureState, loadedFeatureState), is(true));
    }

    @Test
    public void testEnabledStateSavingAndLoading() {
        FeatureState savedFeatureState = new FeatureState(TestFeature.FEATURE).enable();
        stateRepository.setFeatureState(savedFeatureState);

        FeatureState loadedFeatureState = stateRepository.getFeatureState(TestFeature.FEATURE);
        assertThat(loadedFeatureState.isEnabled(), is(true));

        stateRepository.setFeatureState(savedFeatureState.disable());
        loadedFeatureState = stateRepository.getFeatureState(TestFeature.FEATURE);
        assertThat(loadedFeatureState.isEnabled(), is(false));
    }

    private static enum TestFeature implements Feature {
        FEATURE,
    }
}
