package org.togglz.zookeeper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.client.ZooKeeperSaslClient;
import org.junit.After;
import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.activation.UsernameActivationStrategy;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.util.FeatureStateStorageWrapper;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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

    public static final String TEST_ZNODE = "/test/features";
    private ZookeeperStateRepository stateRepository;

    static class ServerClientPair {
        public final TestingServer server;
        public final CuratorFramework client;

        ServerClientPair(TestingServer server, CuratorFramework client) {
            this.server = server;
            this.client = client;
        }
    }

    public static ServerClientPair startServer(Map<String, String> data) throws Exception {

        TestingServer server = new TestingServer();
        CuratorFramework client;
        // for environments where sasl is set, null it our for our test
        System.setProperty(ZooKeeperSaslClient.ENABLE_CLIENT_SASL_KEY, "false");

        client = CuratorFrameworkFactory.builder().connectString(server.getConnectString()).retryPolicy(new RetryOneTime(2000)).build();
        client.start();

        for (Map.Entry<String, String> initialData : data.entrySet()) {
            client.create().creatingParentContainersIfNeeded().forPath(initialData.getKey(), initialData.getValue().getBytes("UTF-8"));
        }
        return new ServerClientPair(server, client);
    }

    public static void stopServer(ServerClientPair pair) throws IOException {
        pair.client.close();
        pair.server.stop();
        System.clearProperty(ZooKeeperSaslClient.ENABLE_CLIENT_SASL_KEY);
    }

    ServerClientPair serverClientPair;

    public void setupTestWithEmptyDatastore() throws Exception {
        setupTestWithData(Collections.EMPTY_MAP);
    }

    public void setupTestWithData(Map<String,String> initialParameters) throws Exception {
        serverClientPair = startServer(initialParameters);
        stateRepository = ZookeeperStateRepository.newBuilder(serverClientPair.client, TEST_ZNODE).build();
    }

    @After
    public void cleanUp() throws Exception {
        stopServer(serverClientPair);
    }

    @Test
    public void testFeatureSavingAndLoading() throws Exception {
        setupTestWithEmptyDatastore();
        assertNull(stateRepository.getFeatureState(TestFeature.FEATURE));
        stateRepository.setFeatureState(new FeatureState(TestFeature.FEATURE));

        assertNotNull(stateRepository.getFeatureState(TestFeature.FEATURE));
    }

    @Test
    public void testActivationStrategySavingAndLoading() throws Exception {
        setupTestWithEmptyDatastore();
        FeatureState savedFeatureState = new FeatureState(TestFeature.FEATURE);
        savedFeatureState.setStrategyId(UsernameActivationStrategy.ID);
        savedFeatureState.setParameter(UsernameActivationStrategy.PARAM_USERS, "user1, user2, user3");
        stateRepository.setFeatureState(savedFeatureState);

        FeatureState loadedFeatureState = stateRepository.getFeatureState(TestFeature.FEATURE);

        assertThat(reflectionEquals(savedFeatureState, loadedFeatureState), is(true));
    }

    @Test
    public void testEnabledStateSavingAndLoading() throws Exception {
        setupTestWithEmptyDatastore();
        FeatureState savedFeatureState = new FeatureState(TestFeature.FEATURE).enable();
        stateRepository.setFeatureState(savedFeatureState);

        FeatureState loadedFeatureState = stateRepository.getFeatureState(TestFeature.FEATURE);
        assertThat(loadedFeatureState.isEnabled(), is(true));

        stateRepository.setFeatureState(savedFeatureState.disable());
        loadedFeatureState = stateRepository.getFeatureState(TestFeature.FEATURE);
        assertThat(loadedFeatureState.isEnabled(), is(false));
    }

    @Test
    public void testZkNodeChangesUpdateFeatureState() throws Exception {
        setupTestWithEmptyDatastore();
        FeatureState savedFeatureState = new FeatureState(TestFeature.FEATURE);
        savedFeatureState.setStrategyId(UsernameActivationStrategy.ID);
        savedFeatureState.setParameter(UsernameActivationStrategy.PARAM_USERS, "user1, user2, user3");
        stateRepository.setFeatureState(savedFeatureState);

        FeatureState loadedFeatureState = stateRepository.getFeatureState(TestFeature.FEATURE);
        assertThat(reflectionEquals(savedFeatureState, loadedFeatureState), is(true));

        // Modify data in ZK
        FeatureStateStorageWrapper externallySetStateWrapper = new FeatureStateStorageWrapper();
        FeatureState externallySetState = new FeatureState(TestFeature.FEATURE);
        ObjectMapper objectMapper = new ObjectMapper();
        final String json = objectMapper.writeValueAsString(externallySetStateWrapper);

        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverClientPair.client.setData().forPath(TEST_ZNODE + "/FEATURE", json.getBytes("UTF-8"));
                    latch.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        latch.await(2, TimeUnit.SECONDS);
        Thread.sleep(500);

        loadedFeatureState = stateRepository.getFeatureState(TestFeature.FEATURE);
        assertThat(reflectionEquals(externallySetState, loadedFeatureState), is(true));
    }

    @Test
    public void testLoadingWithSavedState() throws Exception {
        // re-setup the server
        Map<String, String> initialData = new HashMap<>();
        initialData.put(TEST_ZNODE + "/FEATURE", "{\"enabled\":true,\"strategyId\":null,\"parameters\":{}}");
        // recreate the zookeeper server with the data we want for this test
        setupTestWithData(initialData);

        FeatureState expectedFeatureState = new FeatureState(TestFeature.FEATURE);
        expectedFeatureState.setEnabled(true);

        FeatureState loadedFeatureState = stateRepository.getFeatureState(TestFeature.FEATURE);
        assertThat(reflectionEquals(expectedFeatureState, loadedFeatureState), is(true));
    }


    private static enum TestFeature implements Feature {
        FEATURE,
    }
}
